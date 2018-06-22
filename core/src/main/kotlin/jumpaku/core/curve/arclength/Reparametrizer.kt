package jumpaku.core.curve.arclength

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.geom.Point
import jumpaku.core.geom.divide
import jumpaku.core.geom.line
import jumpaku.core.geom.middle
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.util.divOption
import org.apache.commons.math3.util.FastMath
import java.util.*



fun Curve.approximateParams(n: Int): Array<Double> = repeatBisect(domain, n) { it ->
    val (p0, p1, p2) = it.sample(3).map { evaluate(it) }
    line(p0, p2).map { p1.dist(it) }.getOrElse { p1.dist(p2) }
}.map { it.begin }.append(domain.end).toArray()

fun repeatBisect(domain: Interval, times: Int = 2, evaluateError: (Interval)->Double): Stream<Interval> {
    val cache = TreeMap<Double, List<Interval>>(naturalOrder())
    cache[evaluateError(domain)] = List.of(domain)
    repeat(times) {
        cache.pollLastEntry().value.forEach { i ->
            val (t0, t1, t2) = i.sample(3)
            val i0 = Interval(t0, t1)
            cache.compute(evaluateError(i0)) { _, v -> if (v != null) v.prepend(i0) else List.of(i0) }
            val i1 = Interval(t1, t2)
            cache.compute(evaluateError(i1)) { _, v -> if (v != null) v.prepend(i1) else List.of(i1) }
        }
    }
    return Stream.ofAll(cache.values.flatten().sortedBy { it.begin })
}

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
        val quadratics: Array<MonotonicQuadratic>) {

    data class MonotonicQuadratic(val b0: Double, val b1: Double, val b2: Double, val domain: Interval): (Double)->Double {

        val range: Interval = Interval(b0, b2)

        init {
            require(b1 in b0..b2) { "not monotonic (b0, b1, c2) = ($b0, $b1, $b2), domain($domain)" }
        }

        override fun invoke(t: Double): Double {
            require(t in domain) { "t($t) is out of domain($domain)" }
            val (t0, t2) = domain
            val u = ((t - t0)/(t2 - t0)).coerceIn(0.0, 1.0)
            return b0.divide(u, b1).divide(u, b1.divide(u, b2)).coerceIn(range)
        }

        fun invert(s: Double): Double {
            require(s in range) { "s($s) is out of range($range)" }

            fun f(t: Double): Double = b0.divide(t, b1).divide(t, b1.divide(t, b2))
            fun dfdt(t: Double): Double = (b1 - b0).divide(t, b2 - b1)*2

            tailrec fun newton(u0: Double = 0.5, times: Int = 20, tolerance: Double = 1.0e-5): Double {
                val u1 = (f(u0) - s).divOption(dfdt(u0)).map { u0 - it }
                return when {
                    u1.isEmpty -> if (u0 < 0.5) 0.0 else 1.0
                    FastMath.abs(u1.get() - u0) <= tolerance || times == 1 -> u1.get()
                    else -> newton(u1.get(), times - 1, tolerance)
                }
            }

            return domain.let { (t0, t2) -> t0.divide(newton(), t2) }.coerceIn(domain)
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

        fun interpolateQuadraticArcLength(curve: Curve, t0: Double, t2: Double): MonotonicQuadratic {
            val domain = Interval(t0, t2)
            val (p0, p1, p2) = domain.sample(3).map { curve(it) }
            val s0 = 0.0
            val (s1, s2) = quadraticArcLength(p0, p1, p2).map { l0, l1 -> Tuple2(l0, l0 + l1) }
            val b1 = (2*s1 - s0.middle(s2)).coerceIn(s0, s2)
            return MonotonicQuadratic(s0, b1, s2, domain)
        }

        /**
         * integrate |B'(t)| = 2*sqrt(a)*sqrt(u(t)^2 + A)) for t in [0, 0.5] and [0.5, 1]
         */
        fun quadraticArcLength(b0: Point, p1: Point, b2: Point): Tuple2<Double, Double> {
            val b1 = p1.divide(-1.0, b0.middle(b2))
            val v0 = b1 - b0
            val v1 = b2 - b1
            val a = (v1 - v0).square()
            val A = v1.cross(v0).square().divOption(a*a)
            val abs_dB0 = 2*v0.length()
            val abs_dB1 = (v0 + v1).length()
            val abs_dB2 = 2*v1.length()
            val u0 = v0.dot(v1 - v0).divOption(a)
            val u1 = (v1 - v0).dot(v1 + v0).divOption(2*a)
            val u2 = (v1 - v0).dot(v1).divOption(a)

            if (arrayOf(A, u0, u1, u2).any { it.isEmpty }) return Tuple2(v0.length(), v1.length())

            val sqrta = FastMath.sqrt(a)
            fun integrate(begin: Double, end: Double, beginB: Double, endB: Double): Option<Double> {
                val leftTop = FastMath.sqrt(a)*(end*end + begin*begin + A.get())*(end + begin)
                val leftBottom = end*endB + begin*beginB
                val right = A.get()*FastMath.log(FastMath.abs((2*sqrta*end + endB) / (2*sqrta*begin + beginB)))
                return leftTop.divOption(leftBottom).map { sqrta*(it + right) }
            }

            return Tuple2(integrate(u0.get(), u1.get(), abs_dB0, abs_dB1),
                    integrate(u1.get(), u2.get(), abs_dB1, abs_dB2)
            ).map({ it.getOrElse { v0.length() }.coerceAtLeast(0.0) },
                    { it.getOrElse { v1.length() }.coerceAtLeast(0.0) })
        }

        fun of(curve: Curve, originalParams: Array<Double>): Reparametrizer {
            val qs = originalParams.zipWithNext { t0, t2 -> Reparametrizer.interpolateQuadraticArcLength(curve, t0, t2) }
            val arcLengthParams = originalParams.foldIndexed(List.empty<Double>()) { i, ss, t ->
                if (i == 0) List.of(0.0) else ss.prepend(ss.head() + qs[i - 1](t))
            }.reverse().toArray()
            val quadratics = qs.mapIndexed { i, q ->
                val l = arcLengthParams[i]
                q.copy(b0 = q.b0 + l, b1 = q.b1 + l, b2 = q.b2 + l) }
            return Reparametrizer(originalParams, arcLengthParams, Array.ofAll(quadratics))
        }
    }
}