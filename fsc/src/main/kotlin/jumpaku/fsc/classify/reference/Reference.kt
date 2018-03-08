package jumpaku.fsc.classify.reference

import io.vavr.API
import jumpaku.core.affine.Point
import jumpaku.core.affine.times
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.clamp
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import kotlin.math.absoluteValue


interface Reference {

    fun isValidFor(fsc: FuzzyCurve, nFmps: Int = 15): Grade
}

interface ReferenceCurve : FuzzyCurve {
    val conicSection: ConicSection
}


fun evaluateWithoutDomain(cs: ConicSection, t: Double): Point {
    val wt = RationalBezier.bezier1D(t, API.Array(1.0, cs.weight, 1.0))
    val (p0, p1, p2) = cs.representPoints.map { it.toVector() }
    val p = ((1 - t) * (1 - 2 * t) * p0 + 2 * t * (1 - t) * (1 + cs.weight) * p1 + t * (2 * t - 1) * p2) / wt
    val (r0, r1, r2) = cs.representPoints.map { it.r }
    val r = listOf(
            r0 * (1 - t) * (1 - 2 * t) / wt,
            r1 * 2 * (cs.weight + 1) * t * (1 - t) / wt,
            r2 * t * (2 * t - 1) / wt
    ).map { it.absoluteValue }.sum()

    return Point(p, r)
}

fun reference(fsc: ArcLengthReparametrized, s0: Double, s1: Double, cs: ConicSection): ReferenceCurve {
    val csReparametrized = cs.reparametrizeArcLength()
    val l1 = csReparametrized.arcLength()
    val l0 = l1 * s0 / (s1 - s0)
    val l2 = l1 * (fsc.arcLength() - s1) / (s1 - s0)

    fun isLinear(cs: ConicSection): Boolean = cs.center().isEmpty

    fun changeParameter(s: Double): Double = when {
        isLinear(cs) -> (s - l0) / l1
        else -> {
            val complementReparametrized = cs.complement().reparametrizeArcLength()
            val lc = complementReparametrized.arcLength()
            val lRound = l1 + lc

            (s - l0).absoluteValue.rem(lRound).let { l ->
                val p = when {
                    s < l0 -> when {
                        l < lc -> complementReparametrized.toOriginalParam(l)
                        else -> csReparametrized.toOriginalParam(clamp(lRound - l, csReparametrized.domain))
                    }
                    else -> when {
                        l < l1 -> csReparametrized.toOriginalParam(l)
                        else -> complementReparametrized.toOriginalParam(clamp(lRound - l, complementReparametrized.domain))
                    }
                }
                if ((s < l0 && l < lc) || (s >= l0 && l >= l1)) p / (2 * p - 1) else p
            }
        }
    }

    return object : ReferenceCurve {

        override val domain: Interval = Interval(0.0, l0 + l1 + l2)

        override val conicSection: ConicSection = cs

        override fun evaluate(t: Double): Point = evaluateWithoutDomain(cs, changeParameter(t))
    }
}
