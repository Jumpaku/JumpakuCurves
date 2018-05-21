package jumpaku.fsc.classify.reference

import io.vavr.API
import io.vavr.Tuple
import io.vavr.Tuple3
import jumpaku.core.geom.line
import jumpaku.core.geom.plane
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.*
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane


class Elliptic(polyline: Polyline, nSamples: Int) : Reference(polyline) {
    override val conicSection: ConicSection by lazy {
        val (b, e) = domain
        val f = EllipticGenerator.computeEllipticFar(this, b, e, nSamples)
        val w = EllipticGenerator.computeEllipticWeight(this, b, e, f, nSamples)
        ConicSection(polyline(b), polyline(f), polyline(e), w)
    }
}

class EllipticGenerator(val nSamples: Int = 25) : ReferenceGenerator {
    override fun generate(fsc: FuzzyCurve, t0: Double, t1: Double): Reference {
        val tf = computeEllipticFar(fsc, t0, t1, nSamples)
        val w = computeEllipticWeight(fsc, t0, t1, tf, nSamples)
        val base = ConicSection(fsc(t0), fsc(tf), fsc(t1), w)
        val polyline = ReferenceGenerator.ellipticPolyline(fsc, t0, t1, base)
        return Elliptic(polyline, nSamples)
    }

    fun generateScattered(fsc: FuzzyCurve): Reference {
        val (t0, _, t1) = scatteredEllipticParams(fsc, nSamples)
        return generate(fsc, t0, t1)
    }

    companion object {
        /**
         * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
         */
        fun scatteredEllipticParams(fsc: FuzzyCurve, nSamples: Int): Tuple3<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            return API.For(ts.take(nSamples / 3), ts.drop(2 * nSamples / 3))
                    .yield({ t0, t1 ->
                        val tf = computeEllipticFar(fsc, t0, t1, nSamples)
                        API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
                    })
                    .maxBy { (_, area) -> area }
                    .map { it._1() }.get()
        }

        /**
         * Computes a far point which bisects triangle area.
         * Far point on the fsc is a point such that line segment(f, m) bisects an area surrounded by an elliptic arc(fsc(t0), fsc(t1)) and a line segment(fsc(t0), fsc(t1)),
         * where f is far point, m is the middle point between fsc(t0) and fsc(t1).
         */
        fun computeEllipticFar(fsc: FuzzyCurve, t0: Double, t1: Double, nSamples: Int): Double {
            val middle = fsc(t0).middle(fsc(t1))
            val ts = Interval(t0, t1).sample(nSamples)
            val ps = ts.map(fsc)
            val areas = ps.zipWith(ps.tail(), middle::area).scanLeft(0.0, Double::plus)
            val index = areas.lastIndexWhere { it < areas.last() / 2 }

            val relative = 1.0e-2
            val absolute = 1.0e-2
            return BrentSolver(relative, absolute).solve(50, {
                val m = fsc(it)
                val l = areas[index] + middle.area(ps[index], m)
                val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1], m)
                l - r
            }, ts[index], ts[index + 1])
        }

        fun computeEllipticWeight(fsc: FuzzyCurve, t0: Double, t1: Double, tf: Double, nSamples: Int): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val far = fsc(tf)

            val xy_xx = API.For(plane(begin, far, end), line(begin, end), fsc.domain.sample(nSamples))
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
            if (xy_xx.isEmpty) return 0.999

            return xy_xx.unzip { it }.apply { xy, xx ->
                xy.sum().toDouble().divOrElse(xx.sum().toDouble(), 0.999).coerceIn(-0.999, 0.999)
            }
        }
    }
}