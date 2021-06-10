package jumpaku.curves.core.curve.arclength

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler

interface ParamConverter {

    val domain: Interval

    val range: Interval

    operator fun invoke(t: Double): Double

    operator fun invoke(sampler: Sampler): List<Double> = invoke(sampler.sample(domain))

    operator fun invoke(sortedParams: List<Double>): List<Double> = sortedParams.map { invoke(it) }

    fun restrict(begin: Double, end: Double): ParamConverter {
        require(begin < end) { "must be begin($begin) < end($end)" }
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }
        return restrict(Interval(begin, end))
    }

    fun restrict(interval: Interval): ParamConverter = object : ParamConverter {
        override val domain: Interval = interval
        override val range: Interval = Interval(this@ParamConverter(interval.begin), this@ParamConverter(interval.end))

        override fun invoke(t: Double): Double = this@ParamConverter.invoke(t)
    }

}
