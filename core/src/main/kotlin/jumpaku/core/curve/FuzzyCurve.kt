package jumpaku.core.curve

import jumpaku.core.affine.Point
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.fuzzy.Grade


interface FuzzyCurve : Curve {

    val reparametrized: ArcLengthReparametrized

    fun reparametrizeArcLength(): ArcLengthReparametrized = reparametrized

    fun toCrisp(): Curve = object : FuzzyCurve {
        override val reparametrized: ArcLengthReparametrized by lazy {
            ArcLengthReparametrized(this, 100)
        }
        override val domain: Interval = this@FuzzyCurve.domain
        override fun evaluate(t: Double): Point = this@FuzzyCurve.evaluate(t).toCrisp()
    }

    fun isPossible(other: FuzzyCurve, n: Int): Grade {
        val selfSamples = reparametrizeArcLength().evaluateAll(n)
        val otherSamples = other.reparametrizeArcLength().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isPossible).reduce(Grade::and)
    }

    fun isNecessary(other: FuzzyCurve, n: Int): Grade {
        val selfSamples = reparametrizeArcLength().evaluateAll(n)
        val otherSamples = other.reparametrizeArcLength().evaluateAll(n)
        return selfSamples.zipWith(otherSamples, Point::isNecessary).reduce(Grade::and)
    }
}