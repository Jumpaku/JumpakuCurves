package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear

class ClassifierPrimitive7(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val pL = Linear.of(reparametrized).isValidFor(reparametrized, nFmps)
        val pC = Circular.of(reparametrized, nSamples).isValidFor(reparametrized, nFmps)
        val pE = Elliptic.of(reparametrized, nSamples).isValidFor(reparametrized, nFmps)
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