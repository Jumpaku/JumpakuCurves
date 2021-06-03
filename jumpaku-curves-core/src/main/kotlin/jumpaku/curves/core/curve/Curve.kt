package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point

interface Curve : (Double) -> Point {

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

    fun evaluateAll(n: Int): List<Point> = evaluateAll(domain.sample(n))

    fun evaluateAll(delta: Double): List<Point> = evaluateAll(domain.sample(delta))

    fun evaluateAll(sortedParams:List<Double>): List<Point> = sortedParams.map { evaluate(it) }

    fun sample(n: Int): List<ParamPoint> = sample(domain.sample(n))

    fun sample(delta: Double): List<ParamPoint> = sample(domain.sample(delta))

    fun sample(sortedParams:List<Double>): List<ParamPoint> = evaluateAll(sortedParams).zip(sortedParams, ::ParamPoint)

    fun toCrisp(): Curve = object : Curve {
        override val domain: Interval = this@Curve.domain
        override fun evaluate(t: Double): Point = this@Curve.evaluate(t).toCrisp()
    }
}
