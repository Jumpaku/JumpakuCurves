package jumpaku.fsc.fragment

import io.vavr.Tuple2
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2

data class Chunk(
        val interval: Interval,
        val necessity: Grade,
        val possibility: Grade
) {

    fun state(threshold: TruthValueThreshold): State = when {
        (necessity < threshold.necessity && possibility < threshold.possibility) -> State.MOVE
        (threshold.necessity < necessity && threshold.possibility < possibility) -> State.STAY
        else -> State.UNKNOWN
    }

    enum class State {
        STAY,
        MOVE,
        UNKNOWN
    }
}

fun chunk(fsc: BSpline, interval: Interval, n: Int): Chunk {
    val pointTimeSeries = fsc.restrict(interval).evaluateAll(n)
    val last = pointTimeSeries.last()
    val (ns, ps) = pointTimeSeries.unzip { Tuple2(last.isNecessary(it), last.isPossible(it)) }
    return Chunk(interval, ns.reduce(Grade::and), ps.reduce(Grade::and))
}