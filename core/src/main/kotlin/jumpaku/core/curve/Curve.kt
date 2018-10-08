package jumpaku.core.curve

import jumpaku.core.geom.Point
import jumpaku.core.geom.line
import jumpaku.core.util.orDefault

interface Curve : (Double)->Point {

    val domain: Interval

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point

    override operator fun invoke(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        return evaluate(t)
    }

    fun evaluateAll(n: Int): List<Point> = domain.sample(n).map(this::evaluate)

    fun evaluateAll(delta: Double): List<Point> = domain.sample(delta).map(this::evaluate)

    fun sample(n: Int): List<ParamPoint> = domain.sample(n).map { ParamPoint(this(it), it) }

    fun sample(delta: Double): List<ParamPoint> = domain.sample(delta).map { ParamPoint(this(it), it) }

    fun toCrisp(): Curve = object : Curve {
        override val domain: Interval = this@Curve.domain
        override fun evaluate(t: Double): Point = this@Curve.evaluate(t).toCrisp()
    }
}
