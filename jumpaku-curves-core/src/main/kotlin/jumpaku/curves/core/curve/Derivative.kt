package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Vector

interface Derivative : (Double) -> Vector {

    val domain: Interval

    override operator fun invoke(t: Double): Vector

    operator fun invoke(sampler: Sampler): List<Vector> = invoke(sampler.sample(domain))

    operator fun invoke(sortedParams: List<Double>): List<Vector> = sortedParams.map { invoke(it) }

    fun restrict(subDomain: Interval): Derivative = DerivativeRestriction(this, subDomain)

    fun restrict(begin: Double, end: Double): Derivative = restrict(Interval(begin, end))
}