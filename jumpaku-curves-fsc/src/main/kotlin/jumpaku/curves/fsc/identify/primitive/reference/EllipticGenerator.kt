package jumpaku.curves.fsc.identify.primitive.reference

import jumpaku.commons.control.orDefault
import jumpaku.commons.control.result
import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Line
import jumpaku.curves.core.geom.Plane
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.line
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.apache.commons.math3.analysis.solvers.BrentSolver


class EllipticGenerator(val nSamples: Int = 25) : ReferenceGenerator {

    override fun <C : Curve> generate(fsc: ReparametrizedCurve<C>, t0: Double, t1: Double): Reference {
        val s = fsc.originalCurve
        val tf = computeEllipticFar(s, t0, t1, nSamples)
        val w = computeEllipticWeight(s, t0, t1, tf, s.domain, nSamples)
        val base = reparametrize(ConicSection(s(t0), s(tf), s(t1), w))
        val complement = reparametrize(base.originalCurve.complement())
        val domain = fsc.run {
            ReferenceGenerator.ellipticDomain(toArcLengthRatio(t0), toArcLengthRatio(t1), base, complement, nSamples)
        }
        return Reference(base.originalCurve, domain)
    }

    fun <C : Curve> generateScattered(fsc: ReparametrizedCurve<C>): Reference {
        val (t0, _, t1) = scatteredEllipticParams(fsc.originalCurve, nSamples)
        return generate(fsc, t0, t1)
    }

    fun <C : Curve> generateBeginEnd(fsc: ReparametrizedCurve<C>): Reference {
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val tf = computeEllipticFar(s, t0, t1, nSamples)
        val w = computeEllipticWeight(s, t0, t1, tf, s.domain, nSamples)
        val base = ConicSection(s(t0), s(tf), s(t1), w)
        return Reference(base, Interval.Unit)
    }

    companion object {
        /**
         * Computes parameters which maximizes triangle area of (fsc(t0), fsc(far), fsc(t1)).
         */
        fun scatteredEllipticParams(fsc: Curve, nSamples: Int): Triple<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            val result = mutableListOf<Pair<Triple<Double, Double, Double>, Double>>()
            for (t0 in ts.take(nSamples / 3)){
                for (t1 in ts.drop(2 * nSamples / 3)) {
                    val tf = computeEllipticFar(fsc, t0, t1, nSamples)
                    result += Pair(Triple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))


                }
            }
            return result.maxByOrNull { (_, area) -> area }?.first!!
        }

        /**
         * Computes a far point which bisects triangle area.
         * Far point on the fsc is a point such that line segment(f, m) bisects an area surrounded by an elliptic arc(fsc(t0), fsc(t1)) and a line segment(fsc(t0), fsc(t1)),
         * where f is far point, m is the middle point between fsc(t0) and fsc(t1).
         */
        fun computeEllipticFar(fsc: Curve, t0: Double, t1: Double, nSamples: Int): Double {
            val middle = fsc(t0).middle(fsc(t1))
            val ts = Interval(t0, t1).sample(nSamples)
            val ps = ts.map(fsc)
            val areas = ps.zipWithNext(middle::area).scan(0.0, Double::plus)
            //asVavr().scanLeft(0.0, Double::plus)
            val index = areas.indexOfLast { it < areas.last() / 2 }

            val t = BrentSolver(1.0e-6).solve(50, {
                val m = fsc(ts[index].lerp(it, ts[index + 1]))
                val l = areas[index] + middle.area(ps[index], m)
                val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1], m)
                l - r
            }, 0.0, 1.0)
            return ts[index].lerp(t, ts[index + 1])
        }

        fun computeEllipticWeight(
            fsc: Curve, t0: Double, t1: Double, tf: Double, rangeSamples: Interval, nSamples: Int
        ): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val far = fsc(tf)

            val xy_xx = result {
                val plane = Plane(begin, far, end)
                val line = Line(begin, end)
                rangeSamples.sample(nSamples).map { tp ->
                    val p = fsc(tp).projectTo(plane)
                    val a = far.projectTo(line(p, p + (end - begin)).orThrow())
                    val b = far.projectTo(line)
                    val t = (a - far).dot(b - far).tryDiv(b.distSquare(far)).value().orDefault(0.0)
                    val x = far.lerp(t, begin.middle(end))
                    val dd = x.distSquare(p)
                    val ll = begin.distSquare(end) / 4
                    val yi = dd + t * t * ll - 2 * t * ll
                    val xi = ll * t * t - dd
                    val wi = 1.0//FastMath.exp(-fsc(tp).r)

                    Pair(wi * yi * xi, wi * xi * xi)
                }
            }.value().orNull() ?: return 0.999

            return xy_xx.sumOf { it.first }
                .divOrDefault(xy_xx.sumOf { it.second }) { 0.999 }
                .coerceIn(-0.999, 0.999)
        }
    }
}