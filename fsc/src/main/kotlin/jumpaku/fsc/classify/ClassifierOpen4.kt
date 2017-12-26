package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val muL = Linear.ofBeginEnd(reparametrized).isValidFor(reparametrized, nFmps)
        val muC = Circular.ofBeginEnd(reparametrized).isValidFor(reparametrized, nFmps)
        val muE = Elliptic.ofBeginEnd(reparametrized, nSamples).isValidFor(reparametrized, nFmps)

        return ClassifyResult(
                CurveClass.LineSegment to (muL),
                CurveClass.CircularArc to (!muL and muC),
                CurveClass.EllipticArc to (!muL and !muC and muE),
                CurveClass.OpenFreeCurve to (!muL and !muC and !muE)
        )
    }
}