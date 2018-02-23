package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val reparametrized = fsc.reparametrizeArcLength()
        val pL = Linear.ofBeginEnd(reparametrized).isValidFor(reparametrized, nFmps)
        val pC = Circular.ofBeginEnd(reparametrized).isValidFor(reparametrized, nFmps)
        val pE = Elliptic.ofBeginEnd(reparametrized, nSamples).isValidFor(reparametrized, nFmps)

        return ClassifyResult(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE)
        )
    }
}