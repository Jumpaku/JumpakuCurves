package jumpaku.core.curve.arclength

import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.util.asKt
import jumpaku.core.util.asVavr

/**
 * maps arc-length ratio parameter to point on original curve.
 */
class ReparametrizedCurve<C : Curve>(val originalCurve: C, val reparametrizer: Reparametrizer): Curve {

    constructor(originalCurve: C, originalParams: List<Double>)
            : this(originalCurve, Reparametrizer.of(originalCurve, originalParams))

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
        val i0 = reparametrizer.originalParams.asVavr().search(t0).let { if (it < 0) -it-1 else it }
        val i1 = reparametrizer.originalParams.asVavr().search(t1).let { if (it < 0) -it-1 else it }
        return ReparametrizedCurve(originalCurve, reparametrizer.originalParams.asVavr().slice(i0, i1).prepend(t0).append(t1).asKt())
    }

    fun <O: Curve> isPossible(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n).zip(other.evaluateAll(n), Point::isPossible).reduce(Grade::and)

    fun <O: Curve> isNecessary(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n).zip(other.evaluateAll(n), Point::isNecessary).reduce(Grade::and)
}