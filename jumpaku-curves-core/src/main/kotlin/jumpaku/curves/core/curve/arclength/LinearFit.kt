package jumpaku.curves.core.curve.arclength

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.geom.lerp
import kotlin.math.max

class LinearFit(samples: List<Pair<Double, Double>>) : ParamConverter {

    init {
        require(samples.size >= 2)
    }

    val originalParams = samples.map { it.first }

    val targetParams = samples.map { it.second }

    override val domain: Interval = originalParams.run { Interval(first(), last()) }

    override val range: Interval = targetParams.run { Interval(first(), last()) }

    override operator fun invoke(t: Double): Double {
        require(t in domain) { "t($t) must be in $domain" }
        val (b, e) = domain
        if (t == b) return targetParams.first()
        if (t == e) return targetParams.last()
        val index = originalParams.binarySearch(t).let { max(it, -it - 2) }
        return convert(t, originalParams, targetParams, index)
    }

    override operator fun invoke(sampler: Sampler): List<Double> = invoke(sampler.sample(domain))

    override operator fun invoke(sortedParams: List<Double>): List<Double> {
        var index = 0
        val us = mutableListOf<Double>()
        for (t in sortedParams) {
            require(t in domain)
            if (t == domain.begin) {
                us += targetParams.first()
                continue
            }
            if (t == domain.end) {
                us += targetParams.last()
                continue
            }
            if (originalParams.size > sortedParams.size) {
                index = originalParams.binarySearch(t)
                    .let { max(it, -it - 2) }
                    .coerceAtMost(originalParams.lastIndex - 1)
            }
            while (index < originalParams.lastIndex - 1 && originalParams[index + 1] < t) ++index
            us += convert(t, originalParams, targetParams, index)
        }
        return us
    }

    override fun restrict(begin: Double, end: Double): LinearFit {
        require(begin < end) { "must be begin($begin) < end($end)" }
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }
        return restrict(Interval(begin, end))
    }

    override fun restrict(interval: Interval): LinearFit {
        require(interval in domain) { "$interval is out of domain($domain)" }
        val (t0, t1) = interval
        val i0 = originalParams.binarySearch(t0).let { Integer.max(-it - 1, it) }
        val i1 = originalParams.binarySearch(t1).let { Integer.max(-it - 1, it) }
        val s0 = invoke(t0)//.coerceAtMost(originalParams[i0])
        val s1 = invoke(t1)//.coerceAtLeast(originalParams[i1])
        val restrictedOriginalParams = listOf(t0) + originalParams.subList(i0, i1) + listOf(t1)
        val restrictedTargetParams = (listOf(s0) + targetParams.subList(i0, i1) + listOf(s1))
            .map { (it - s0) / (s1 - s0).coerceIn(Interval.Unit) }
        return LinearFit(restrictedOriginalParams.zip(restrictedTargetParams))
    }

    companion object {

        private fun convert(t: Double, params: List<Double>, target: List<Double>, index: Int): Double {
            val t0 = params[index]
            val t1 = params[index + 1]
            val u0 = target[index]
            val u1 = target[index + 1]
            return u0.lerp((t - t0) / (t1 - t0), u1).coerceIn(u0, u1)
        }
    }

}