package jumpaku.core.curve.arclength

import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.geom.line
import jumpaku.core.geom.middle
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.util.divOption
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.util.FastMath
import kotlin.math.max
import kotlin.math.min


fun repeatBisect(curve: Curve, tolerance: Double, domain: Interval = curve.domain): Stream<Interval> =
        repeatBisect(curve, domain) { subDomain ->
            val (p0, p1, p2) = subDomain.sample(3).map { curve(it) }
            line(p0, p2).map { p1.dist(it) }.getOrElse { p1.dist(p0) } > tolerance
        }

fun repeatBisect(curve: Curve, domain: Interval = curve.domain, shouldBisect: (Interval)->Boolean): Stream<Interval> =
        domain.sample(3)
                .let { (t0, t1, t2) -> Stream.of(Interval(t0, t1), Interval(t1, t2)) }
                .flatMap { subDomain ->
                    if (shouldBisect(subDomain)) repeatBisect(curve, subDomain, shouldBisect)
                    else  Stream.of(subDomain)
                }

class Reparametrizer private constructor(
        val originalParams: Array<Double>,
        val arcLengthParams: Array<Double>,
        val quadratics: Array<MonotonicLinear>) {

    data class MonotonicLinear(val a: Double, val b: Double, val domain: Interval): (Double)->Double {

        val range: Interval

        init {
            require(a >= 0.0) { "not monotonic (a, b, c) = ($a, $b), domain($domain)" }
            range = domain.let { (t0, t2) -> Interval(a*t0 + b, a*t2 + b) }
        }

        override fun invoke(t: Double): Double {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return (a*t + b).coerceIn(range)
        }

        fun invert(s: Double): Double {
            require(s in range) { "s($s) is out of range($range)" }
            val (t0, t2) = domain
            return (s-b).divOption(a).getOrElse { t0.middle(t2) }.coerceIn(domain)
        }
    }

    init {
        require(originalParams.size() > 1) { "originalToArcLength.size() is too small"}
        require(arcLengthParams.size() == originalParams.size()) { "arcLengthParams.size() != originalParams.size()" }
        require(quadratics.size() == originalParams.size() - 1) { "quadratics.size() != originalParams.size() - 1" }
    }

    val domain: Interval = Interval(originalParams.head(), originalParams.last())

    val range: Interval = Interval(arcLengthParams.head(), arcLengthParams.last())

    fun toOriginal(arcLengthParam: Double): Double {
        require(arcLengthParam in range) { "arcLengthParam($arcLengthParam) is out of range($range)"}
        val i = arcLengthParams.search(arcLengthParam).let { if (it < 0) -it-1 else it }
        return if (i == 0) originalParams[0]
        else quadratics[i-1].let { it.invert(arcLengthParam.coerceIn(it.range)).coerceIn(domain) }
    }

    fun toArcLength(originalParam: Double): Double {
        require(originalParam in domain) { "originalParam($originalParam) is out of domain($domain)"}
        val i = originalParams.search(originalParam).let { if (it < 0) -it-1 else it }
        return if (i == 0) 0.0
        else quadratics[i-1].let { it(originalParam.coerceIn(it.domain)).coerceIn(range) }
    }

    companion object {

        fun interpolateLinear(curve: Curve, t0: Double, t1: Double): MonotonicLinear {
            val p0 = curve(t0)
            val p1 = curve(t1)
            val s0 = 0.0
            val s1 = p0.dist(p1)
            val a = (s0 - s1).divOption(t0 - t1).getOrElse { 0.0 }.coerceAtLeast(0.0)
            val b = (-t1*s0 + t0*s1).divOption(t0 - t1).getOrElse { s0.middle(s1) }
            return MonotonicLinear(a, b, Interval(t0, t1))
        }

        fun of(curve: Curve, originalParams: Array<Double>): Reparametrizer {
            val ls = originalParams.zipWithNext { t0, t2 -> Reparametrizer.interpolateLinear(curve, t0, t2) }
            val arcLengthParams = originalParams.foldIndexed(List.empty<Double>()) { i, ss, t ->
                if (i == 0) List.of(0.0) else ss.prepend(ss.head() + ls[i - 1](t))
            }.reverse().toArray()
            val quadratics = ls.mapIndexed { i, q -> q.copy(b = q.b + arcLengthParams[i]) }
            return Reparametrizer(originalParams, arcLengthParams, Array.ofAll(quadratics))
        }
    }
}