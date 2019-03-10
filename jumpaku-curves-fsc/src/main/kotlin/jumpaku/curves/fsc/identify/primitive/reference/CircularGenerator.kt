package jumpaku.curves.fsc.identify.primitive.reference

import io.vavr.API
import io.vavr.Tuple3
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import jumpaku.curves.core.util.component3
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.apache.commons.math3.analysis.solvers.BrentSolver


class CircularGenerator(val nSamples: Int = 25) : ReferenceGenerator {

    override fun <C : Curve> generate(fsc: ReparametrizedCurve<C>, t0: Double, t1: Double): Reference {
        val s = fsc.originalCurve
        val tf = computeCircularFar(s, t0, t1)
        val base = reparametrize(ConicSection.shearedCircularArc(s(t0), s(tf), s(t1)))
        val complement = reparametrize(base.originalCurve.complement())
        val domain = fsc.reparametrizer.run {
            ReferenceGenerator.ellipticDomain(toArcLengthRatio(t0), toArcLengthRatio(t1), base, complement)
        }
        return Reference(base.originalCurve, domain)
    }

    fun <C : Curve> generateScattered(fsc: ReparametrizedCurve<C>): Reference {
        val s = fsc.originalCurve
        val (t0, _, t1) = scatteredCircularParams(s, nSamples)
        return generate(fsc, t0, t1)
    }

    fun <C : Curve> generateBeginEnd(fsc: ReparametrizedCurve<C>): Reference {
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val tf = computeCircularFar(s, t0, t1)
        val base = ConicSection.shearedCircularArc(s(t0), s(tf), s(t1))
        return Reference(base, Interval.ZERO_ONE)
    }

    companion object {

        /**
         * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
         */
        fun scatteredCircularParams(fsc: Curve, nSamples: Int): Tuple3<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            return API.For(ts.take(nSamples / 3), ts.drop(2 * nSamples / 3))
                    .`yield` { t0, t1 ->
                        val tf = computeCircularFar(fsc, t0, t1)
                        API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
                    }
                    .maxBy { (_, area) -> area }
                    .map { it._1() }.get()
        }

        fun computeCircularFar(fsc: Curve, t0: Double, t1: Double): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val t = BrentSolver(1.0e-6).solve(50, {
                val f = fsc(t0.lerp(it, t1))
                f.distSquare(begin) - f.distSquare(end)
            }, 0.0, 1.0)
            return t0.lerp(t, t1)
        }
    }
}
