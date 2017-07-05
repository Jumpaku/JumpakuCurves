package org.jumpaku.fsc.identify.classify

import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear


class Classifier4 : Classifier {

    override fun classify(fsc: BSpline): Result {
        val muL = Linear.create(fsc.domain.begin, fsc.domain.end, fsc).isValidFor(fsc)
        val muC = Circular.create(fsc.domain.begin, fsc.domain.end, fsc).isValidFor(fsc)
        val muE = Elliptic.create(fsc.domain.begin, fsc.domain.end, fsc).isValidFor(fsc)

        return Result(
                CurveClass.LineSegment to (muL),
                CurveClass.CircularArc to (!muL and muC),
                CurveClass.EllipticArc to (!muL and !muC and muE),
                CurveClass.OpenFreeCurve to (!muL and !muC and !muE)
        )
    }
}