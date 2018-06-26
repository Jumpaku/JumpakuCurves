package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple3
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.analysis.solvers.BrentSolver



class CircularGenerator(val nSamples: Int = 25) : ReferenceGenerator {

    override fun generate(fsc: Curve, t0: Double, t1: Double): Reference {
        val tf = computeCircularFar(fsc, t0, t1)
        val base = ConicSection.shearedCircularArc(fsc(t0), fsc(tf), fsc(t1))
        val (l0, _, l2) = ReferenceGenerator.referenceSubLength(fsc, t0, t1, base)
        val domain = ReferenceGenerator.ellipticDomain(l0, l2, base)
        return Reference(base, domain)
    }

    fun generateScattered(fsc: Curve): Reference {
        val (t0, _, t1) = scatteredCircularParams(fsc, nSamples)
        return generate(fsc, t0, t1)
    }

    fun generateBeginEnd(fsc: Curve): Reference {
        val (t0, t1) = fsc.domain
        val tf = computeCircularFar(fsc, t0, t1)
        val base = ConicSection.shearedCircularArc(fsc(t0), fsc(tf), fsc(t1))
        return Reference(base, Interval.ZERO_ONE)
    }

    companion object {

        /**
         * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
         */
        fun scatteredCircularParams(fsc: Curve, nSamples: Int): Tuple3<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            return API.For(ts.take(nSamples / 3), ts.drop(2 * nSamples / 3))
                    .yield({ t0, t1 ->
                        val tf = computeCircularFar(fsc, t0, t1)
                        API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
                    })
                    .maxBy { (_, area) -> area }
                    .map { it._1() }.get()
        }

        fun computeCircularFar(fsc: Curve, t0: Double, t1: Double): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val relative = 1.0e-9
            val absolute = 1.0e-7
            return BrentSolver(relative, absolute).solve(50, {
                val f = fsc(it)
                f.distSquare(begin) - f.distSquare(end)
            }, t0, t1)
        }
    }
}
