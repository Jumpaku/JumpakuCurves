package jumpaku.core.curve.arclength

import io.vavr.collection.Array
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point

/**
 * maps arc-length ratio parameter to point on original curve.
 */
class ReparametrizedCurve<C : Curve>(val originalCurve: C, originalParams: Array<Double>): Curve {

    val reparametrizer: Reparametrizer = Reparametrizer.of(originalCurve, originalParams)

    val chordLength: Double = reparametrizer.chordLength

    override val domain: Interval = Interval.ZERO_ONE

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)"}
        return originalCurve(reparametrizer.toOriginal(t.coerceIn(reparametrizer.range)))
    }

    fun <O: Curve> isPossible(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n).zipWith(other.evaluateAll(n), Point::isPossible).reduce(Grade::and)

    fun <O: Curve> isNecessary(other: ReparametrizedCurve<O>, n: Int): Grade =
            evaluateAll(n).zipWith(other.evaluateAll(n), Point::isNecessary).reduce(Grade::and)
}