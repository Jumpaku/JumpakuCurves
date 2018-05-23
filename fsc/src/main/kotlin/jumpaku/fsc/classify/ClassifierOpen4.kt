package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.reference.*


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val pL = fsc.isPossible(LinearGenerator(nSamples).generate(fsc), nFmps)
        val pC = fsc.isPossible(CircularGenerator(nSamples).generate(fsc), nFmps)
        val pE = fsc.isPossible(EllipticGenerator(nSamples).generate(fsc), nFmps)

        return ClassifyResult(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE)
        )
    }
}