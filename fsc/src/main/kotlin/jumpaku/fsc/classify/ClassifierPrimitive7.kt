package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.hashMap
import jumpaku.fsc.classify.Classifier.Companion.isClosed
import jumpaku.fsc.classify.reference.*

class ClassifierPrimitive7(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val refL = LinearGenerator(nSamples).generate(fsc)
        val refC = CircularGenerator(nSamples).generateScattered(fsc)
        val refE = EllipticGenerator(nSamples).generateScattered(fsc)
        val pL = fsc.isPossible(refL, nFmps)
        val pC = fsc.isPossible(refC, nFmps)
        val pE = fsc.isPossible(refE, nFmps)
        val pClosed = isClosed(fsc)
        val grades = hashMap(
                CurveClass.LineSegment to (pL),
                CurveClass.Circle to (pClosed and !pL and pC),
                CurveClass.CircularArc to (!pClosed and !pL and pC),
                CurveClass.Ellipse to (pClosed and !pL and !pC and pE),
                CurveClass.EllipticArc to (!pClosed and !pL and !pC and pE),
                CurveClass.ClosedFreeCurve to (pClosed and !pL and !pC and !pE),
                CurveClass.OpenFreeCurve to (!pClosed and !pL and !pC and !pE))
        return ClassifyResult(grades, refL, refC, refE)
    }
}