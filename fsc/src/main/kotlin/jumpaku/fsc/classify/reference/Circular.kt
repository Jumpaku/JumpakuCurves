package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple3
import jumpaku.core.affine.Point
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.analysis.solvers.BrentSolver


class Circular(val conicSection: ConicSection, val domain: Interval) : Reference {

    val reference: FuzzyCurve = object : FuzzyCurve {

        override val domain: Interval = this@Circular.domain

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return evaluateWithoutDomain(t, conicSection)
        }
    }

    override fun isValidFor(fsc: BSpline): Grade = reference.isPossible(fsc)

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: FuzzyCurve): Circular {
            val tf = computesCircularFar(t0, t1, fsc)
            val circular = ConicSection.shearedCircularArc(fsc(t0), fsc(tf), fsc(t1))
            val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), circular)

            return Circular(circular, domain)
        }

        fun ofBeginEnd(fsc: BSpline): Circular = ofParams(fsc.domain.begin, fsc.domain.end, fsc)

        fun of(fsc: BSpline): Circular {
            val (t0, _, t1) = scatteredCircularParams(fsc)

            return ofParams(t0, t1, fsc)
        }
    }
}

fun computesCircularFar(t0: Double, t1: Double, fsc: FuzzyCurve): Double {
    val begin = fsc(t0)
    val end = fsc(t1)
    val relative = 1.0e-8
    val absolute = 1.0e-5
    return BrentSolver(relative, absolute).solve(50, {
        val f = fsc(it)
        f.distSquare(begin) - f.distSquare(end)
    }, t0, t1)
}

/**
 * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
 */
fun scatteredCircularParams(fsc: BSpline, nSamples: Int = 99): Tuple3<Double, Double, Double> {
    val ts = fsc.domain.sample(nSamples)
    return API.For(ts.take(nSamples/3), ts.drop(2*nSamples/3))
            .yield({ t0, t1 ->
                val tf = computesCircularFar(t0, t1, fsc)
                API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
            })
            .maxBy { (_, area) -> area }
            .map { it._1() }.get()
}
