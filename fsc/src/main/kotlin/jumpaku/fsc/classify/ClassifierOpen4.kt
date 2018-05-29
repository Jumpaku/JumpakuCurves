package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.hashMap
import jumpaku.fsc.classify.reference.*


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val s = fsc.reparameterized
        val refL = LinearGenerator(nSamples).generateBeginEnd(s)
        val refC = CircularGenerator(nSamples).generateBeginEnd(s)
        val refE = EllipticGenerator(nSamples).generateBeginEnd(s)
        val pL = fsc.isPossible(refL.base, nFmps)
        val pC = fsc.isPossible(refC.base, nFmps)
        val pE = fsc.isPossible(refE.base, nFmps)
        val grades = hashMap(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE))
        return ClassifyResult(grades, refL, refC, refE)
    }
}