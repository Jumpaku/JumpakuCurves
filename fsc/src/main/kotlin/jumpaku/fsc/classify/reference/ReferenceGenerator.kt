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

        fun linearPolyline(fsc: Curve, t0: Double, t1: Double, base: ConicSection, nSamples: Int): Polyline {
            val (l0, l1, l2) = referenceSubLength(fsc, t0, t1, base)
            fun linearEvaluate(s: Double): Point {
                val t = (s - l0) / l1
                val wt = RationalBezier.bezier1D(t, API.Array(1.0, base.weight, 1.0))
                val (p0, p1, p2) = base.representPoints.map { it.toVector() }
                val p = ((1 - t) * (1 - 2 * t) * p0 + 2 * t * (1 - t) * (1 + base.weight) * p1 + t * (2 * t - 1) * p2) / wt
                val (r0, r1, r2) = base.representPoints.map { it.r }
                val r = listOf(
                        r0 * (1 - t) * (1 - 2 * t) / wt,
                        r1 * 2 * (base.weight + 1) * t * (1 - t) / wt,
                        r2 * t * (2 * t - 1) / wt
                ).map { it.absoluteValue }.sum()

                return Point(p, r)
            }
            return Polyline(Interval(0.0, l0 + l1 + l2).sample(nSamples).map { linearEvaluate(it) })
        }

        fun ellipticPolyline(fsc: Curve, t0: Double, t1: Double, base: ConicSection): Polyline {
            val (l0, l1, l2) = referenceSubLength(fsc, t0, t1, base)
            val reparametrized = base.reparameterized
            val reparametrizedC = base.complement().reparameterized
            val round = l1 + reparametrizedC.arcLength()
            fun ellipticEvaluate(s: Double): Point {
                val ss = (s - l0).rem(round).let { if (it > 0) it else it + round }
                return when(ss) {
                    in reparametrized.domain -> reparametrized(ss)
                    else -> reparametrizedC((round - ss).coerceIn(reparametrizedC.domain))
                }
            }
            val ps = chordalParametrize(Array.of(reparametrized.polyline, (reparametrizedC.polyline.reverse())).flatMap { it.points })
            val length = l0 + l1 + l2
            val n0 = FastMath.floor(l0/round).toInt()
            val front = listOf(ellipticEvaluate(0.0)) + (ps.filter { it.param > (n0 + 1)*round - l0 } + (0 until n0).flatMap { ps }).map { it.point }
            val n2 = FastMath.floor((length - l0)/round).toInt()
            val back = ((0 until n2).flatMap { ps } + ps.filter { it.param < length - n2*round - l0 }).map { it.point } + ellipticEvaluate(length)
            return Polyline((front + back).asIterable())
        }
    }
}
