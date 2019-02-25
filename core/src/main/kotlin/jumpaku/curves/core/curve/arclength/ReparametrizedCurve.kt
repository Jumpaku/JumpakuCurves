package jumpaku.curves.core.curve.arclength

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.line
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.core.util.orDefault

/**
 * maps arc-length ratio parameter to point on original curve.
 */
class ReparametrizedCurve<C : Curve>(val originalCurve: C, val reparametrizer: Reparametrizer): Curve {

    val chordLength: Double = reparametrizer.chordLength

    override val domain: Interval = Interval.ZERO_ONE

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)"}
        return originalCurve(reparametrizer.toOriginal(t.coerceIn(reparametrizer.range)))
    }

    fun restrict(begin: Double, end: Double): ReparametrizedCurve<C> {
        require(begin < end) { "must be begin($begin) < end($end)" }
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }
        return restrict(Interval(begin, end))
    }

    fun restrict(interval: Interval): ReparametrizedCurve<C> {
        require(interval in domain) { "Interval($interval) is out of domain($domain)" }
        val (s0, s1) = interval
        val t0 = reparametrizer.toOriginal(s0)
        val t1 = reparametrizer.toOriginal(s1)
        val i0 = reparametrizer.originalParams.asVavr()
                .search(t0).let { if (it < 0) -it-1 else it }
        val i1 = reparametrizer.originalParams.asVavr()
                .search(t1).let { if (it < 0) -it-1 else it }
        val params = listOf(t0) + reparametrizer.originalParams.slice(i0 until i1) + listOf(t1)
        return ReparametrizedCurve.of(originalCurve, params)
    }

    fun <O: Curve> isPossible(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n)
                    .zip(other.evaluateAll(n), Point::isPossible)
                    .reduce(Grade::and)

    fun <O: Curve> isNecessary(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n)
                    .zip(other.evaluateAll(n), Point::isNecessary)
                    .reduce(Grade::and)

    companion object {

        fun <C: Curve> of(originalCurve: C, originalParams: Iterable<Double>): ReparametrizedCurve<C> =
                ReparametrizedCurve(originalCurve, Reparametrizer.of(originalCurve, originalParams))

        fun <C: Curve> approximate(curve: C, tolerance: Double): ReparametrizedCurve<C> =
                of(curve, repeatBisect(curve, tolerance).map { it.begin } + curve.domain.end)

        fun repeatBisect(curve: Curve, tolerance: Double): Iterable<Interval> =
                repeatBisect(curve) { subDomain ->
                    val (p0, p1, p2) = subDomain.sample(3).map { curve(it) }
                    line(p0, p2).tryMap { p1.dist(it) }.value().orDefault { p1.dist(p0) } > tolerance
                }

        fun repeatBisect(curve: Curve, shouldBisect: (Interval)->Boolean): Iterable<Interval> =
                repeatBisectImpl(curve, curve.domain, shouldBisect)

        private fun repeatBisectImpl(
                curve: Curve,
                domain: Interval,
                shouldBisect: (Interval)->Boolean
        ): Iterable<Interval> = domain.sample(3)
                    .let { (t0, t1, t2) -> listOf(Interval(t0, t1), Interval(t1, t2)) }
                    .flatMap { subDomain ->
                        if (shouldBisect(subDomain)) repeatBisectImpl(curve, subDomain, shouldBisect)
                        else listOf(subDomain)
                    }
    }
}