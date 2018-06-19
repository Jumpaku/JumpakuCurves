package jumpaku.fsc.classify.reference

import io.vavr.Tuple3
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.divOrElse
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.util.FastMath


interface ReferenceGenerator {

    fun generate(fsc: Curve, t0: Double = fsc.domain.begin, t1: Double = fsc.domain.end): Reference

    companion object {

        fun referenceSubLength(fsc: Curve, t0: Double, t1: Double, base: ConicSection): Tuple3<Double, Double, Double> {
            val reparametrized = fsc.reparameterized
            val s0 = reparametrized.reparametrizer.toArcLength(t0)
            val s1 = reparametrized.reparametrizer.toArcLength(t1)
            val s = reparametrized.domain.end
            val l1 = base.reparameterized.domain.end
            val l0 = (l1 * s0 / (s1 - s0))
            val l2 = (l1 * (s - s1) / (s1 - s0))
            return Tuple3(l0, l1, l2)
        }

        fun linearDomain(l0: Double, l2: Double, base: ConicSection): Interval {
            val c = base.complement().reverse()
            val s = BrentSolver(1.0e-2, 1.0e-3)
            val b = s.solve(50, { (c(it) - c(1.0)).length() - l0 }, 0.501, 1.0, 0.9)
                    .coerceIn(Interval.ZERO_ONE)
            val e = s.solve(50, { (c(it) - c(0.0)).length() - l2 }, 0.0, 0.499, 0.1)
                    .coerceIn(Interval.ZERO_ONE)
            return Interval(b - 1, e + 1)
        }

        fun ellipticDomain(l0: Double, l2: Double, base: ConicSection): Interval {
            val rC = base.complement().reverse().reparameterized
            val lc = rC.domain.end
            val b = rC.run { reparametrizer.toOriginal((lc - l0).coerceIn(domain)) }
                    .coerceIn(Interval.ZERO_ONE)
            val e = rC.run { reparametrizer.toOriginal(l2.coerceIn(domain)) }
                    .coerceIn(Interval.ZERO_ONE)
            return Interval(b - 1, e + 1)
        }
    }
}
