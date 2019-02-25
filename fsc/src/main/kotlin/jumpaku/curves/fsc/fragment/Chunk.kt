package jumpaku.curves.fsc.fragment

import io.vavr.Tuple2
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2

data class Chunk(
        val interval: Interval,
        val necessity: Grade,
        val possibility: Grade) {

    fun state(threshold: Fragmenter.Threshold): State = when {
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
    val (ns, ps) = pointTimeSeries.asVavr().unzip { Tuple2(last.isNecessary(it), last.isPossible(it)) }
    return Chunk(interval, ns.reduce(Grade::and), ps.reduce(Grade::and))
}