package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.divide
import jumpaku.fsc.classify.Classifier.Companion.isClosed
import jumpaku.fsc.classify.reference.CircularGenerator
import jumpaku.fsc.classify.reference.EllipticGenerator
import jumpaku.fsc.classify.reference.LinearGenerator

class ClassifierPrimitive7(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val pL = fsc.isPossible(LinearGenerator(nSamples).generate(fsc), nFmps)
        val pC = fsc.isPossible(CircularGenerator(nSamples).generateScattered(fsc), nFmps)
        val pE = fsc.isPossible(EllipticGenerator(nSamples).generateScattered(fsc), nFmps)
        val pClosed = isClosed(fsc)
        return ClassifyResult(
                CurveClass.LineSegment to (pL),
                CurveClass.Circle to (pClosed and !pL and pC),
                CurveClass.CircularArc to (!pClosed and !pL and pC),
                CurveClass.Ellipse to (pClosed and !pL and !pC and pE),
                CurveClass.EllipticArc to (!pClosed and !pL and !pC and pE),
                CurveClass.ClosedFreeCurve to (pClosed and !pL and !pC and !pE),
                CurveClass.OpenFreeCurve to (!pClosed and !pL and !pC and !pE)
        )
    }
}