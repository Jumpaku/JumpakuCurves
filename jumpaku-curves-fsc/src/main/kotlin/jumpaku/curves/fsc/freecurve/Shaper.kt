package jumpaku.curves.fsc.freecurve

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.identify.primitive.Open4Identifier

class Shaper(val segmenter: Segmenter = Segmenter(identifier = Open4Identifier(nSamples = 25, nFmps = 15)),
             val smoother: Smoother = Smoother(pruningFactor = 2.0, nFitSamples = 17, fscSampleSpan = 0.02),
             val sampleFsc: (BSpline) -> List<Double>) {

    fun shape(fsc: BSpline): Triple<List<Double>, SegmentResult, SmoothResult> {
        val ts = sampleFsc(fsc)
        val segmentResult = segmenter.segment(fsc, ts)
        val smoothResult = smoother.smooth(fsc, ts, segmentResult)
        return Triple(ts, segmentResult, smoothResult)
    }
}