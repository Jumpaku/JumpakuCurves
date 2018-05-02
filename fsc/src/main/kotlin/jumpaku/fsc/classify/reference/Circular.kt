package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple3
import jumpaku.core.affine.Point
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.analysis.solvers.BrentSolver


class CircularReferenceGenerator : ReferenceGenerator {
    override fun generate(fsc: FuzzyCurve, t0: Double, t1: Double): ReferenceCurve {
        val tf = computeCircularFar(fsc, t0, t1)
        val base = ConicSection.shearedCircularArc(fsc(t0), fsc(tf), fsc(t1))
        val (l0, l1, l2) = ReferenceGenerator.referenceSubLength(fsc, t0, t1, base)
        val eval = ReferenceGenerator.ellipticReferenceEvaluator(fsc, t0, t1, base)
        return object : ReferenceCurve() {
            override val domain: Interval = Interval(0.0, l0 + l1 + l2)
            override val conicSection: ConicSection by lazy {
                val (b, e) = domain
                val f = eval(computeCircularFar(this, b, e))
                ConicSection.shearedCircularArc(eval(b), f, eval(e))
            }

            override fun evaluate(t: Double): Point = eval(t)
        }
    }

    fun generateScattered(fsc: FuzzyCurve, nSamples: Int): ReferenceCurve {
        val (t0, _, t1) = scatteredCircularParams(fsc, nSamples)
        return generate(fsc, t0, t1)
    }

    companion object {
        /**
         * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
         */
        fun scatteredCircularParams(fsc: FuzzyCurve, nSamples: Int): Tuple3<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            return API.For(ts.take(nSamples / 3), ts.drop(2 * nSamples / 3))
                    .yield({ t0, t1 ->
                        val tf = computeCircularFar(fsc, t0, t1)
                        API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
                    })
                    .maxBy { (_, area) -> area }
                    .map { it._1() }.get()
        }

        fun computeCircularFar(fsc: FuzzyCurve, t0: Double, t1: Double): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val relative = 1.0e-8
            val absolute = 1.0e-5
            return BrentSolver(relative, absolute).solve(50, {
                val f = fsc(it)
                f.distSquare(begin) - f.distSquare(end)
            }, t0, t1)
        }
    }
}
