package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.reference.*


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val pL = reparametrized.isPossible(LinearGenerator(nSamples).generate(reparametrized), nFmps)
        val pC = reparametrized.isPossible(CircularGenerator(nSamples).generate(reparametrized), nFmps)
        val pE = reparametrized.isPossible(EllipticGenerator(nSamples).generate(reparametrized), nFmps)

        return ClassifyResult(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE)
        )
    }
}