package jumpaku.curves.core.curve.arclength

import jumpaku.commons.control.orDefault
import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.middle
import jumpaku.curves.core.util.asVavr
import org.apache.commons.math3.util.FastMath
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3

class Reparametrizer private constructor(
        val originalParams: List<Double>,
        private val arcLengthParams: List<Double>,
        private val quadratics: List<MonotonicQuadratic>) {

    init {
        require(originalParams.size > 1) { "originalToArcLength.size() is too small" }
        require(arcLengthParams.size == originalParams.size) { "arcLengthParams.size() != originalParams.size()" }
        require(quadratics.size == originalParams.size - 1) { "quadratics.size() != originalParams.size() - 1" }
        require(originalParams.all { it.isFinite() }) { "originalParams contains infinite value" }
        require(arcLengthParams.all { it.isFinite() }) { "arcLengthParams contains infinite value" }
    }

    val domain: Interval = Interval(originalParams.first(), originalParams.last())

    val range: Interval = Interval.ZERO_ONE

    val chordLength: Double = arcLengthParams.last()

    fun toOriginal(arcLengthRatio: Double): Double {
        require(arcLengthRatio in range) { "arcLengthRatio($arcLengthRatio) is out of range($range)" }

        val s = (arcLengthRatio * chordLength).coerceIn(0.0..chordLength)
        val i = arcLengthParams.asVavr()
                .search(s)
                .let { if (it < 0) -it - 1 else it }
        return if (i == 0) originalParams[0]
        else quadratics[i - 1].invert(s).coerceIn(domain)
    }

    fun toArcLengthRatio(originalParam: Double): Double {
        require(originalParam in domain) { "originalParam($originalParam) is out of domain($domain)" }

        val i = originalParams.asVavr()
                .search(originalParam)
                .let { if (it < 0) -it - 1 else it }
        return if (i == 0) 0.0
        else quadratics[i - 1].invoke(originalParam).tryDiv(chordLength).value()
                .orDefault { 0.0 }
                .coerceIn(range)
    }

    companion object {

        private fun interpolate(curve: Curve, t0: Double, t2: Double): MonotonicQuadratic {
            val domain = Interval(t0, t2)
            val (p0, p1, p2) = domain.sample(3).map { curve(it) }
            val s0 = 0.0
            val s1 = p0.dist(p1)
            val s2 = s1 + p1.dist(p2)
            val b1 = (2 * s1 - s0.middle(s2)).coerceIn(s0, s2)
            return MonotonicQuadratic(s0, b1, s2, domain)
        }

        fun of(curve: Curve, originalParams: Iterable<Double>): Reparametrizer {
            require(originalParams.all { it.isFinite() }) { "originalParams contains infinite value" }
            val params = originalParams.toList()
            require(params.size > 1) { "originalToArcLength.size() is too small" }

            val qs = params.zipWithNext { t0, t2 -> interpolate(curve, t0, t2) }
            val arcLengthParams = mutableListOf(0.0)
            qs.zip(params.drop(1)) { q, t -> q(t) }.forEach {
                arcLengthParams += (arcLengthParams.last() + it)
            }
            val quadratics = qs.zip(arcLengthParams) { q, l ->
                q.copy(b0 = q.b0 + l, b1 = q.b1 + l, b2 = q.b2 + l)
            }
            return Reparametrizer(params, arcLengthParams, quadratics)
        }
    }
}


data class MonotonicQuadratic(val b0: Double, val b1: Double, val b2: Double, val domain: Interval) : (Double) -> Double {

    val range: Interval = Interval(b0, b2)

    init {
        require(b1 in b0..b2) { "not monotonic (b0, b1, b2) = ($b0, $b1, $b2), domain($domain)" }
    }

    override fun invoke(t: Double): Double {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val (t0, t2) = domain
        val u = ((t - t0).divOrDefault(t2 - t0) { 0.5 }).coerceIn(0.0, 1.0)
        return b0.lerp(u, b1).lerp(u, b1.lerp(u, b2)).coerceIn(range)
    }

    fun invert(s: Double): Double {
        require(s in range) { "s($s) is out of range($range)" }

        fun f(t: Double): Double = b0.lerp(t, b1).lerp(t, b1.lerp(t, b2))
        fun dfdt(t: Double): Double = (b1 - b0).lerp(t, b2 - b1) * 2

        /**
         * range -> [0, 1]
         */
        tailrec fun newton(u0: Double = 0.5, times: Int = 20, tolerance: Double = 1.0e-5): Double {
            val u1 = (f(u0) - s).tryDiv(dfdt(u0)).tryMap { u0 - it }
            return when {
                u1.isFailure -> if (u0 < 0.5) 0.0 else 1.0
                FastMath.abs(u1.orThrow() - u0) <= tolerance || times == 1 -> u1.orThrow()
                else -> newton(u1.orThrow(), times - 1, tolerance)
            }
        }

        return domain.let { (t0, t2) -> t0.lerp(newton(), t2) }.coerceIn(domain)
    }
}
