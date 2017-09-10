package jumpaku.fsc.fragment

import io.vavr.API.*
import io.vavr.Tuple3
import io.vavr.collection.Array
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.TruthValue
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3


class Fragmenter(
        val threshold: TruthValue = TruthValue(0.4, 0.6),
        val n: Int = 4,
        minStayTime: Double = 0.1
) {
    val samplingSpan: Double

    init {
        require(n > 0) { "k should be k > 0" }
        samplingSpan = minStayTime / n
    }

    fun fragment(fsc: BSpline): FragmentResult {
        val chunks = fsc.domain.sample(samplingSpan)
                .sliding(n)
                .map { chunk(fsc, Interval(it.head(), it.last()), n) }
                .toArray()
        val states = chunks.map { it.state(threshold) }
                .fold(Array.of(Fragmenter.State.STAY)) { l, n -> l.append(l.last().transit(n)) }
                .tail()
                .toArray()
        val fragments = chunks.zip(states)
                .fold(Array.of(Tuple(chunks.head().interval.begin, chunks.head().interval.end, states.head()))) { prev, (nChunk, nState) ->
                    when {
                        (prev.last()._3 != nState) -> prev.append(Tuple(nChunk.interval.begin, nChunk.interval.end, nState))
                        else -> prev.update(prev.size() - 1, Tuple3(prev.last()._1, nChunk.interval.end, prev.last()._3))
                    }
                }
                .map { (begin, end, state) ->
                    when (state) {  // 型推論がうまくいかない
                        State.MOVE -> Fragment(Interval(begin, end), Fragment.Type.IDENTIFICATION)
                        State.STAY -> Fragment(Interval(begin, end), Fragment.Type.PARTITION)
                    }
                }
                .toArray()
        return FragmentResult(fragments)
    }

    private enum class State {
        STAY {
            override fun transit(next: Chunk.State): Fragmenter.State {
                return when (next) {
                    Chunk.State.STAY -> Fragmenter.State.STAY
                    Chunk.State.MOVE -> Fragmenter.State.MOVE
                    Chunk.State.UNKNOWN -> Fragmenter.State.STAY
                }
            }
        },
        MOVE {
            override fun transit(next: Chunk.State): Fragmenter.State {
                return when (next) {
                    Chunk.State.STAY -> Fragmenter.State.STAY
                    Chunk.State.MOVE -> Fragmenter.State.MOVE
                    Chunk.State.UNKNOWN -> Fragmenter.State.MOVE
                }
            }
        };

        abstract fun transit(next: Chunk.State): Fragmenter.State
    }
}