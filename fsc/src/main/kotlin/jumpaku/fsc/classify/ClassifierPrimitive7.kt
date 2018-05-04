package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.Classifier.Companion.isClosed
import jumpaku.fsc.classify.reference.CircularGenerator
import jumpaku.fsc.classify.reference.EllipticGenerator
import jumpaku.fsc.classify.reference.LinearGenerator

class ClassifierPrimitive7(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val pL = LinearGenerator(nSamples).generate(reparametrized).isPossible(reparametrized, nFmps)
        val pC = CircularGenerator(nSamples).generateScattered(reparametrized).isPossible(reparametrized, nFmps)
        val pE = EllipticGenerator(nSamples).generateScattered(reparametrized).isPossible(reparametrized, nFmps)
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