package jumpaku.curves.fsc.freecurve

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.identify.primitive.Open4Identifier

/**
 * Shapes an FSC as a sequence of elliptic arcs using Identifier.
 * The concept of this process is proposed in the following paper:
 * Tomohiko Ito, Hazuki Kamiya, Rin Aburaya, Sato Saga. Free Curve Shaping for Interactive Sketch-based Drawing. Journal of Information Processing 2018;59(12):2288-2298. URL: http://id.nii.ac.jp/1001/00192861/
 */
class Shaper(val segmenter: Segmenter = Segmenter(identifier = Open4Identifier(nSamples = 25, nFmps = 15)),
             val smoother: Smoother = Smoother(pruningFactor = 2.0, samplingFactor = 33),
             val sampler: Sampler) {

    sealed class Sampler {

        abstract operator fun invoke(domain: Interval): List<Double>

        class ByFixedNumber(val nSamples: Int) : Sampler() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(nSamples)
        }

        class ByEqualInterval(val samplingSpan: Double) : Sampler() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(samplingSpan)
        }
    }

    fun shape(fsc: BSpline): Triple<List<Double>, SegmentResult, SmoothResult> {
        val ts = sampler(fsc.domain)
        val segmentResult = segmenter.segment(fsc, ts)
        val smoothResult = smoother.smooth(fsc, ts, segmentResult)
        return Triple(ts, segmentResult, smoothResult)
    }
}

