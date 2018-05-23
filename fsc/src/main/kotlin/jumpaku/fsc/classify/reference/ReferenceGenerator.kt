package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple3
import io.vavr.collection.Array
import jumpaku.core.geom.Point
import jumpaku.core.geom.times
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.geom.chordalParametrize
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.util.divOrElse
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.util.FastMath
import kotlin.math.absoluteValue



interface ReferenceGenerator {

    fun generate(fsc: Curve, t0: Double = fsc.domain.begin, t1: Double = fsc.domain.end): Curve

    companion object {

        fun referenceSubLength(fsc: Curve, t0: Double, t1: Double, base: ConicSection): Tuple3<Double, Double, Double> {
            val reparametrized = fsc.reparameterized
            val s0 = reparametrized.arcLengthUntil(t0)
            val s1 = reparametrized.arcLengthUntil(t1)
            val s = reparametrized.arcLength()
            val l1 = base.reparameterized.arcLength()
            val l0 = (l1 * s0 / (s1 - s0))
            val l2 = (l1 * (s - s1) / (s1 - s0))
            return Tuple3(l0, l1, l2)
        }

        fun linearPolyline(l0: Double, l1: Double, l2: Double, base: ConicSection, nSamples: Int): Polyline {
            val m = base.evaluateAll(nSamples)
            val c = base.complement()
            val s = BrentSolver(1.0e-3, 1.0e-4)
            val end = s.solve(50, { (c(it) - c(0.0)).length() - l0 }, 0.0, 0.499, 0.1)
            val begin = s.solve(50, { (c(it) - c(1.0)).length() - l2 }, 0.501, 1.0, 0.9)
            val f = Interval(0.0, end).sample(FastMath.ceil(nSamples*l1.divOrElse(l0, 2.0)).toInt()).map { c(it) }
            val b = Interval(begin, 1.0).sample(FastMath.ceil(nSamples*l2.divOrElse(l0, 2.0)).toInt()).map { c(it) }
            return Polyline(f + m + b)
        }

        fun ellipticPolyline(l0: Double, l1: Double, l2: Double, base: ConicSection): Polyline {
            val reparametrized = base.reparameterized
            val reparametrizedC = base.complement().reparameterized
            val m = reparametrized.polyline
            val f = reparametrizedC.polyline.run { subdivide(l0.coerceIn(domain))._1.reverse() }
            val b = reparametrizedC.polyline.run { reverse().subdivide(l2.coerceIn(domain))._1 }
            return Polyline(listOf(f, m, b).flatMap { it.points })
        }
    }
}
