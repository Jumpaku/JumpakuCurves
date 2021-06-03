package jumpaku.curves.core.curve

interface Sampler {

    fun sample(domain: Interval): List<Double>

    companion object {

        operator fun invoke(samplingSpan: Double): Sampler = object : Sampler {
            override fun sample(domain: Interval): List<Double> = domain.sample(samplingSpan)
        }

        operator fun invoke(nSamples: Int): Sampler = object : Sampler {
            override fun sample(domain: Interval): List<Double> = domain.sample(nSamples)
        }
    }
}