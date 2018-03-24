package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.API.Array
import io.vavr.API.For
import io.vavr.Tuple
import io.vavr.Tuple3
import jumpaku.core.affine.Point
import jumpaku.core.affine.line
import jumpaku.core.affine.plane
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.*
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane


class Elliptic(val reference: ReferenceCurve) : Reference {

    override fun isValidFor(fsc: FuzzyCurve, nFmps: Int): Grade = reference.isPossible(fsc, nFmps)

    companion object {

        fun ofParams(t0: Double, t1: Double, fsc: FuzzyCurve, nSamples: Int): Elliptic {
            val tf = computeEllipticFar(t0, t1, fsc, nSamples)
            val w = computeEllipticWeight(t0, t1, tf, fsc, nSamples)
            val reparametrized = fsc.reparametrizeArcLength()
            return Elliptic(reference(reparametrized,
                    reparametrized.arcLengthUntil(t0),
                    reparametrized.arcLengthUntil(t1),
                    ConicSection(fsc(t0), fsc(tf), fsc(t1), w)))
        }

        fun ofBeginEnd(fsc: FuzzyCurve, nSamples: Int = 99): Elliptic = ofParams(fsc.domain.begin, fsc.domain.end, fsc, nSamples)

        fun of(fsc: FuzzyCurve, nSamples: Int = 99): Elliptic {
            val (t0, _, t1) = scatteredEllipticParams(fsc, nSamples)

            return ofParams(t0, t1, fsc, nSamples)
        }
    }
}

/**
 * Computes a far point which bisects triangle area.
 * Far point on the fsc is a point such that line segment(f, m) bisects an area surrounded by an elliptic arc(fsc(t0), fsc(t1)) and a line segment(fsc(t0), fsc(t1)),
 * where f is far point, m is the middle point between fsc(t0) and fsc(t1).
 */
fun computeEllipticFar(t0: Double, t1: Double, fsc: FuzzyCurve, samplesCount: Int): Double {
    val middle = fsc(t0).middle(fsc(t1))
    val ts = Interval(t0, t1).sample(samplesCount)
    val ps = ts.map(fsc)
    val areas = ps.zipWith(ps.tail(), middle::area).scanLeft(0.0, Double::plus)
    val index = areas.lastIndexWhere { it < areas.last()/2 }

    val relative = 1.0e-2
    val absolute = 1.0e-2
    return BrentSolver(relative, absolute).solve(50, {
        val m = fsc(it)
        val l = areas[index] + middle.area(ps[index], m)
        val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1], m)
        l - r
    }, ts[index], ts[index + 1])
}

fun computeEllipticWeight(t0: Double, t1: Double, tf: Double, fsc: FuzzyCurve, samplesCount: Int): Double {
    val begin = fsc(t0)
    val end = fsc(t1)
    val far = fsc(tf)

    val xy_xx = For(plane(begin, far, end), line(begin, end), fsc.domain.sample(samplesCount))
            .yield(function3 { plane: Plane, line: Line, tp: Double ->
                val p = fsc(tp).projectTo(plane)
                val a = far.projectTo(line(p, end - begin).get())
                val b = far.projectTo(line)
                val t = (a - far).dot(b - far).divOption(b.distSquare(far)).getOrElse(0.0)
                val x = far.divide(t, begin.middle(end))
                val dd = x.distSquare(p)
                val ll = begin.distSquare(end) / 4
                val yi = dd + t * t * ll - 2 * t * ll
                val xi = ll * t * t - dd
                val wi = 1.0//FastMath.exp(-fsc(tp).r)

                Tuple.of(wi * yi * xi, wi * xi * xi)
            }).toArray()
    if (xy_xx.isEmpty){
        return 0.999
    }
    return xy_xx.unzip { it }
            .apply { xy, xx ->
                clamp(xy.sum().toDouble().divOrElse(xx.sum().toDouble(), 0.999), -0.999, 0.999)
            }
}

/**
 * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
 */
fun scatteredEllipticParams(fsc: FuzzyCurve, nSamples: Int): Tuple3<Double, Double, Double> {
    val ts = fsc.domain.sample(nSamples)
    return API.For(ts.take(nSamples/3), ts.drop(2*nSamples/3))
            .yield({ t0, t1 ->
                val tf = computeEllipticFar(t0, t1, fsc, nSamples)
                API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
            })
            .maxBy { (_, area) -> area }
            .map { it._1() }.get()
}
