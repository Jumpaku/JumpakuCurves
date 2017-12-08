package jumpaku.core.curve

import jumpaku.core.affine.Point
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.fuzzy.Grade


interface FuzzyCurve : Curve {

    fun reparametrizeArcLength(): ArcLengthReparametrized = ArcLengthReparametrized(this, 100)

    fun toCrisp(): Curve = object : FuzzyCurve {
        override val domain: Interval = this@FuzzyCurve.domain
        override fun evaluate(t: Double): Point = this@FuzzyCurve.evaluate(t).toCrisp()
    }

    fun isPossible(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = reparametrizeArcLength().evaluateAll(n)
        val otherSamples = other.reparametrizeArcLength().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isPossible).reduce(Grade::and)
    }

    fun isNecessary(other: FuzzyCurve, n: Int = DEFAULT_FUZZY_MATCHING_POINTS): Grade {
        val selfSamples = reparametrizeArcLength().evaluateAll(n)
        val otherSamples = other.reparametrizeArcLength().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isNecessary).reduce(Grade::and)
    }

    companion object {
        val DEFAULT_FUZZY_MATCHING_POINTS = 30
    }
}