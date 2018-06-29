package jumpaku.core.curve.arclength

import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.geom.divide
import jumpaku.core.geom.line
import jumpaku.core.geom.middle
import jumpaku.core.util.*
import org.apache.commons.math3.util.FastMath
import java.util.*



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
        private val arcLengthParams: Array<Double>,
        private val quadratics: Array<MonotonicQuadratic>) {

    data class MonotonicQuadratic(val b0: Double, val b1: Double, val b2: Double, val domain: Interval): (Double)->Double {

        val range: Interval = Interval(b0, b2)

        init {
            require(b1 in b0..b2) { "not monotonic (b0, b1, b2) = ($b0, $b1, $b2), domain($domain)" }
        }

        override fun invoke(t: Double): Double {
            require(t in domain) { "t($t) is out of domain($domain)" }
            val (t0, t2) = domain
            val u = ((t - t0).divOrElse(t2 - t0, 0.5)).coerceIn(0.0, 1.0)
            return b0.divide(u, b1).divide(u, b1.divide(u, b2)).coerceIn(range)
        }

        fun invert(s: Double): Double {
            require(s in range) { "s($s) is out of range($range)" }

            fun f(t: Double): Double = b0.divide(t, b1).divide(t, b1.divide(t, b2))
            fun dfdt(t: Double): Double = (b1 - b0).divide(t, b2 - b1)*2

            /**
             * range -> [0, 1]
             */
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

    val range: Interval = Interval.ZERO_ONE//(arcLengthParams.head(), arcLengthParams.last())

    val chordLength: Double = arcLengthParams.last()

    fun toOriginal(arcLengthRatio: Double): Double {
        require(arcLengthRatio in range) { "arcLengthRatio($arcLengthRatio) is out of range($range)"}
        val s = (arcLengthRatio*chordLength).coerceIn(0.0..chordLength)
        val i = arcLengthParams.search(s).let { if (it < 0) -it-1 else it }
        return if (i == 0) originalParams[0]
        else quadratics[i-1].invert(s).coerceIn(domain)
    }

    fun toArcLengthRatio(originalParam: Double): Double {
        require(originalParam in domain) { "originalParam($originalParam) is out of domain($domain)"}
        val i = originalParams.search(originalParam).let { if (it < 0) -it-1 else it }
        return if (i == 0) 0.0
        else quadratics[i-1].let { it(originalParam).divOption(chordLength)
                .getOrElse { 0.0 }.coerceIn(range) }
    }

    companion object {

        fun interpolate(curve: Curve, t0: Double, t2: Double): MonotonicQuadratic {
            val domain = Interval(t0, t2)
            val (p0, p1, p2) = domain.sample(3).map { curve(it) }
            val s0 = 0.0
            val s1 = p0.dist(p1)
            val s2 = s1 + p1.dist(p2)
            val b1 = (2*s1 - s0.middle(s2)).coerceIn(s0, s2)
            return MonotonicQuadratic(s0, b1, s2, domain)
        }

        fun of(curve: Curve, originalParams: Array<Double>): Reparametrizer {
            val qs = originalParams.zipWithNext { t0, t2 -> Reparametrizer.interpolate(curve, t0, t2) }
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