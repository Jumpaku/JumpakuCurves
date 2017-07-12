package org.jumpaku.fsc.identify.classify

import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear


class ClassifierOpen4 : Classifier {

    override fun classify(fsc: BSpline): Result {
        val muL = Linear.ofBeginEnd(fsc).isValidFor(fsc)
        val muC = Circular.ofBeginEnd(fsc).isValidFor(fsc)
        val muE = Elliptic.ofBeginEnd(fsc).isValidFor(fsc)

        return Result(
                CurveClass.LineSegment to (muL),
                CurveClass.CircularArc to (!muL and muC),
                CurveClass.EllipticArc to (!muL and !muC and muE),
                CurveClass.OpenFreeCurve to (!muL and !muC and !muE)
        )
    }
}