package jumpaku.fsc.identify.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.identify.reference.Circular
import jumpaku.fsc.identify.reference.Elliptic
import jumpaku.fsc.identify.reference.Linear


class ClassifierOpen4 : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val muL = Linear.ofBeginEnd(fsc).isValidFor(fsc)
        val muC = Circular.ofBeginEnd(fsc).isValidFor(fsc)
        val muE = Elliptic.ofBeginEnd(fsc).isValidFor(fsc)

        return ClassifyResult(
                CurveClass.LineSegment to (muL),
                CurveClass.CircularArc to (!muL and muC),
                CurveClass.EllipticArc to (!muL and !muC and muE),
                CurveClass.OpenFreeCurve to (!muL and !muC and !muE)
        )
    }
}