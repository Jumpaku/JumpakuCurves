package org.jumpaku.fsc.identify.classify

import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear

class ClassifierPrimitive7 : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val muL = Linear.of(fsc).isValidFor(fsc)
        val muC = Circular.of(fsc).isValidFor(fsc)
        val muE = Elliptic.of(fsc).isValidFor(fsc)
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