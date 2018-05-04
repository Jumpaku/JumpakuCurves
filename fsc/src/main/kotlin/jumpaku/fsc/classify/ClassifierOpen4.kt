package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.reference.*


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val pL = LinearGenerator(nSamples).generate(reparametrized).isPossible(reparametrized, nFmps)
        val pC = CircularGenerator(nSamples).generate(reparametrized).isPossible(reparametrized, nFmps)
        val pE = EllipticGenerator(nSamples).generate(reparametrized).isPossible(reparametrized, nFmps)

        return ClassifyResult(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE)
        )
    }
}