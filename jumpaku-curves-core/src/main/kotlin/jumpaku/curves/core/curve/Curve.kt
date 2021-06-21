package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point

interface Curve : (Double) -> Point {

    val domain: Interval

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    override operator fun invoke(t: Double): Point

    operator fun invoke(sampler: Sampler): List<Point> = invoke(sampler.sample(domain))

    operator fun invoke(sortedParams: List<Double>): List<Point> = sortedParams.map { invoke(it) }

    fun sample(sampler: Sampler): List<ParamPoint> = sample(sampler.sample(domain))

    fun sample(sortedParams: List<Double>): List<ParamPoint> = invoke(sortedParams).zip(sortedParams, ::ParamPoint)

    fun toCrisp(): Curve = object : Curve {
        override val domain: Interval = this@Curve.domain
        override fun invoke(t: Double): Point = this@Curve.invoke(t).toCrisp()
    }

    fun restrict(subDomain: Interval): Curve = CurveRestriction(this, subDomain)

    fun restrict(begin: Double, end: Double): Curve = restrict(Interval(begin, end))
}
