package jumpaku.curves.fsc.fragment

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade

/**
 * Fragments an FSC into sequence of stay and move FSCs.
 * The concept of this process is proposed in the following paper:
 * NISHIKAWA, A, SAGA, S, MAEDA, J. Performance improvement of geometric curve sequence recognition in the free-hand curve identifier fsci. Journal of Information Processing 2010;51(2):380–390. URL: https://ci.nii.ac.jp/naid/110007970646/en/
 */
class Fragmenter(
    val threshold: Chunk.Threshold = Chunk.Threshold(
        necessity = Grade(0.35),
        possibility = Grade(0.65)
    ),
    val chunkSize: Int = 4,
    val minStayTimeSpan: Double = 0.04
) {

    private enum class State {

        STAY {
            override fun transit(next: Chunk.Label): State = when (next) {
                Chunk.Label.STAY, Chunk.Label.UNKNOWN -> STAY
                Chunk.Label.MOVE -> MOVE
            }
        },
        MOVE {
            override fun transit(next: Chunk.Label): State = when (next) {
                Chunk.Label.STAY -> STAY
                Chunk.Label.MOVE, Chunk.Label.UNKNOWN -> MOVE
            }
        };

        abstract fun transit(next: Chunk.Label): State
    }

    init {
        require(chunkSize >= 2) { "k should be k >= 2" }
        require(minStayTimeSpan.isFinite()) { "minStayTimeSpan($minStayTimeSpan) must be finite" }
        require(minStayTimeSpan > 0.0) { "minStayTimeSpan($minStayTimeSpan) must be positive" }
        require(threshold.necessity <= threshold.necessity) {
            "must be necessityThreshold(${threshold.necessity}) <= possibilityThreshold(${threshold.necessity})"
        }
    }

    fun fragment(fsc: BSpline): List<Fragment> {
        val samplingSpan = minStayTimeSpan / chunkSize
        val chunks = fsc.domain.sample(samplingSpan)
            .windowed(chunkSize)
            .map { Chunk(fsc.restrict(it.first(), it.last()).sample(Sampler(chunkSize))) }
        val states = chunks
            .map { it.label(threshold) }
            .fold(mutableListOf(State.STAY)) { l, n -> l.apply { add(l.last().transit(n)) } }
            .drop(1)
        val initial = chunks.first().run { Triple(beginParam, endParam, states.first()) }
        return chunks.zip(states)
            .fold(mutableListOf(initial)) { prev, (nextChunk, nextState) ->
                prev.apply {
                    val (prevBegin, _, prevState) = last()
                    if (prevState != nextState) add(Triple(nextChunk.beginParam, nextChunk.endParam, nextState))
                    else set(prev.lastIndex, Triple(prevBegin, nextChunk.endParam, prevState))
                }
            }.map { (begin, end, state) ->
                when (state) {
                    State.MOVE -> Fragment(Interval(begin, end), Fragment.Type.Move)
                    State.STAY -> Fragment(Interval(begin, end), Fragment.Type.Stay)
                }
            }
    }
}

