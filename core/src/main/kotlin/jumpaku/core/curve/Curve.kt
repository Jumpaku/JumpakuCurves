package jumpaku.core.curve

import io.vavr.collection.Array
import jumpaku.core.geom.Point
import jumpaku.core.curve.arclength.ArcLengthReparameterized
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.ParamPoint


interface FuzzyCurve : (Double)->Point {

    val domain: Interval

    override operator fun invoke(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        return evaluate(t)
    }

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point

    fun evaluateAll(n: Int): Array<Point> = domain.sample(n).map(this::evaluate)

    fun evaluateAll(delta: Double): Array<Point> = domain.sample(delta).map(this::evaluate)

    fun sample(n: Int): Array<ParamPoint> = domain.sample(n).map { ParamPoint(this(it), it) }

    fun sample(delta: Double): Array<ParamPoint> = domain.sample(delta).map { ParamPoint(this(it), it) }
    val reparameterized: ArcLengthReparameterized

    fun reparametrizeArcLength(): ArcLengthReparameterized = reparameterized

    fun toCrisp(): FuzzyCurve = object : FuzzyCurve {
        override val reparameterized: ArcLengthReparameterized by lazy {
            ArcLengthReparameterized(this, 100)
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