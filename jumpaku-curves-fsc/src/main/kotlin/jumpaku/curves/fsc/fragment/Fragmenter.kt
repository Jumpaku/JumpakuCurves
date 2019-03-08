package jumpaku.curves.fsc.fragment

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import java.util.*


class Fragmenter(
        val threshold: Threshold = Threshold(0.35, 0.65),
        val chunkSize: Int = 4,
        minStayTime: Double = 0.04
) {
    val samplingSpan: Double

    init {
        require(chunkSize > 0) { "k should be k > 0" }
        samplingSpan = minStayTime / chunkSize
    }

    class Threshold(val necessity: Grade, val possibility: Grade) {

        constructor(necessity: Double, possibility: Double) : this(Grade(necessity), Grade(possibility))
    }

    fun fragment(fsc: BSpline): List<Fragment> {
        val chunks = fsc.domain.sample(samplingSpan)
                .windowed(chunkSize)
                .map { chunk(fsc, Interval(it.first(), it.last()), chunkSize) }
        val states = chunks
                .map { it.state(threshold) }
                .fold(mutableListOf(Fragmenter.State.STAY)) { l, n -> l.apply { add(l.last().transit(n)) } }
                .drop(1)
        val initial = Triple(chunks.first().interval.begin, chunks.first().interval.end, states.first())
        return chunks.zip(states)
                .fold(mutableListOf(initial)) { prev, (nChunk, nState) ->
                    val (pBegin, _, pState) = prev.last()
                    if (pState == nState) {
                        prev[prev.lastIndex] = Triple(pBegin, nChunk.interval.end, pState)
                        prev
                    } else {
                        prev += (Triple(nChunk.interval.begin, nChunk.interval.end, nState))
                        prev
                    }
                }.map { (begin, end, state) ->
                    when (state!!) {  // 型推論がうまくいかない
                        State.MOVE -> Fragment(Interval(begin, end), Fragment.Type.Move)
                        State.STAY -> Fragment(Interval(begin, end), Fragment.Type.Stay)
                    }
                }
    }

    private enum class State {
        STAY {
            override fun transit(next: Chunk.State): Fragmenter.State = when (next) {
                Chunk.State.STAY, Chunk.State.UNKNOWN -> Fragmenter.State.STAY
                Chunk.State.MOVE -> Fragmenter.State.MOVE
            }
        },
        MOVE {
            override fun transit(next: Chunk.State): Fragmenter.State = when (next) {
                Chunk.State.STAY -> Fragmenter.State.STAY
                Chunk.State.MOVE, Chunk.State.UNKNOWN -> Fragmenter.State.MOVE
            }
        };

        abstract fun transit(next: Chunk.State): Fragmenter.State
    }
}
