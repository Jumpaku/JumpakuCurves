package jumpaku.fsc.freecurve

import jumpaku.core.curve.bspline.BSpline

class Shaper(val segmenter: Segmenter = Segmenter(identify = Segmenter.defaultIdentifier),
             val smoother: Smoother = Smoother(pruningFactor = 2.0, nFitSamples = 17, fscSampleSpan = 0.02),
             val sampleFsc: (BSpline) -> List<Double>) {

    fun shape(fsc: BSpline): Triple<List<Double>, SegmentResult, SmoothResult> {
        val ts = sampleFsc(fsc)
        val segmentResult = segmenter.segment(fsc, ts)
        val smoothResult = smoother.smooth(fsc, ts, segmentResult)
        return Triple(ts, segmentResult, smoothResult)
    }
}