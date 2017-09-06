package jumpaku.fsc.fragment

import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.fuzzy.TruthValue

data class Chunk(
        val interval: Interval,
        val necessity: Grade,
        val possibility: Grade
) {

    fun state(threshold: TruthValue): State {
        return when {
            (necessity < threshold.necessity && possibility < threshold.possibility) -> State.MOVE
            (threshold.necessity < necessity && threshold.possibility < possibility) -> State.STAY
            else -> State.UNKNOWN
        }
    }

    enum class State {
        STAY,
        MOVE,
        UNKNOWN
    }
}

fun chunk(fsc: BSpline, interval: Interval, n: Int): Chunk {
    val pointTimeSeries = fsc.restrict(interval).evaluateAll(n)
    val tvs = pointTimeSeries.dropRight(1).map {
        val last = pointTimeSeries.last()
        TruthValue(last.isNecessary(it), last.isPossible(it))
    }
    val necessity = tvs.map { it.necessity }.min().getOrElse(Grade.TRUE)
    val possibility = tvs.map { it.possibility }.min().getOrElse(Grade.TRUE)
    return Chunk(interval, necessity, possibility)
}