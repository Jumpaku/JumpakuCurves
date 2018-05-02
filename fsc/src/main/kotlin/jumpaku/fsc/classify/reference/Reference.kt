package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple
import io.vavr.Tuple3
import jumpaku.core.affine.Point
import jumpaku.core.affine.line
import jumpaku.core.affine.plane
import jumpaku.core.affine.times
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.util.*
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import kotlin.math.absoluteValue


abstract class ReferenceCurve : FuzzyCurve {

    abstract val conicSection: ConicSection

    override val reparametrized: ArcLengthReparametrized by lazy {
        ArcLengthReparametrized(this, 100)
    }
}

interface ReferenceGenerator {
    fun generate(fsc: FuzzyCurve, t0: Double = fsc.domain.begin, t1: Double = fsc.domain.end): ReferenceCurve

    companion object {

        fun conicSectionWithoutDomain(cs: ConicSection, t: Double): Point {
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

        /**
         * Changes from conic section arc-length parameter into conic section original parameter in [0, 1]
         */
        fun changeLinearParam(l0: Double, l1: Double): (Double)->Double = { s -> (s - l0) / l1 }

        /**
         * Changes from conic section arc-length parameter into conic section original parameter in [0, 1]
         */
        fun changeEllipticParam(l0: Double, l1: Double, base: ConicSection): (Double)->Double {
            val csReparametrized = base.reparametrized
            val complementReparametrized = base.complement().reparametrized
            val lc = complementReparametrized.arcLength()
            val lRound = l1 + lc
            fun changeEllipticParam(s: Double): Double {
                val l = (s - l0).absoluteValue.rem(lRound)
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
                return if ((s < l0 && l < lc) || (s >= l0 && l >= l1)) p / (2 * p - 1) else p

            }
            return ::changeEllipticParam
        }
        fun referenceSubLength(fsc: FuzzyCurve, t0: Double, t1: Double, base: ConicSection): Tuple3<Double, Double, Double> {
            val reparametrized = fsc.reparametrized
            val s0 = reparametrized.arcLengthUntil(t0)
            val s1 = reparametrized.arcLengthUntil(t1)
            val l1 = base.reparametrized.arcLength()
            val l0 = l1 * s0 / (s1 - s0)
            val l2 = l1 * (reparametrized.arcLength() - s1) / (s1 - s0)
            return Tuple3(l0, l1, l2)
        }
        fun ellipticReferenceEvaluator(fsc: FuzzyCurve, t0: Double, t1: Double, base: ConicSection): (Double)->Point{
            val (l0, l1, _) = referenceSubLength(fsc, t0, t1, base)
            val changeEllipticParam = ReferenceGenerator.changeEllipticParam(l0, l1, base)
            return { t -> conicSectionWithoutDomain(base, changeEllipticParam(t)) }
        }
    }
}
