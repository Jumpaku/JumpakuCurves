package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear

class ClassifierPrimitive7(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val muL = Linear.of(reparametrized).isValidFor(reparametrized)
        val muC = Circular.of(reparametrized, nSamples).isValidFor(reparametrized, nFmps)
        val muE = Elliptic.of(reparametrized, nSamples).isValidFor(reparametrized, nFmps)
        val muClosed = isClosed(fsc)
        return ClassifyResult(
                CurveClass.LineSegment to (muL),
                CurveClass.Circle to (muClosed and !muL and muC),
                CurveClass.CircularArc to (!muClosed and !muL and muC),
                CurveClass.Ellipse to (muClosed and !muL and !muC and muE),
                CurveClass.EllipticArc to (!muClosed and !muL and !muC and muE),
                CurveClass.ClosedFreeCurve to (muClosed and !muL and !muC and !muE),
                CurveClass.OpenFreeCurve to (!muClosed and !muL and !muC and !muE)
        )
    }
}