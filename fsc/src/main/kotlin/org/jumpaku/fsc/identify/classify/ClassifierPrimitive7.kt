package org.jumpaku.fsc.identify.classify

import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear
import org.jumpaku.fsc.identify.reference.mostFarPointOnFsc


class ClassifierPrimitive7 : Classifier {

    override fun classify(fsc: BSpline): Result {
        val t0 = fsc.domain.begin
        val t1 = mostFarPointOnFsc(t0, fsc)
        val muL = Linear.create(t0, fsc.domain.end, fsc).isValidFor(fsc)
        val muC = Circular.create(t0, t1, fsc).isValidFor(fsc)
        val muE = Elliptic.create(t0, t1, fsc).isValidFor(fsc)
        val muClosed = fsc.evaluateAll(2).run { head().isPossible(last()) }
        return Result(
                CurveClass.LineSegment to (muL),
                CurveClass.Circle to (muClosed and (!muL) and muC),
                CurveClass.CircularArc to ((!muClosed) and (!muL) and muC),
                CurveClass.Ellipse to (muClosed and (!muL) and (!muC) and muE),
                CurveClass.EllipticArc to ((!muClosed) and (!muL) and (!muC) and muE),
                CurveClass.ClosedFreeCurve to (muClosed and (!muL) and (!muC) and (!muE)),
                CurveClass.OpenFreeCurve to ((!muClosed) and (!muL) and (!muC) and (!muE))
        )
    }
}