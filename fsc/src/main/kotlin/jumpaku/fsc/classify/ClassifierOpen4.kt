package jumpaku.fsc.classify

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.hashMap
import jumpaku.fsc.classify.reference.*


class ClassifierOpen4(val nSamples: Int = 25, val nFmps: Int = 15) : Classifier {

    override fun classify(fsc: BSpline): ClassifyResult {
        val refL = LinearGenerator(nSamples).generate(fsc)
        val refC = CircularGenerator(nSamples).generate(fsc)
        val refE = EllipticGenerator(nSamples).generate(fsc)
        val pL = fsc.isPossible(refL, nFmps)
        val pC = fsc.isPossible(refC, nFmps)
        val pE = fsc.isPossible(refE, nFmps)
        val grades = hashMap(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE))
        val refs = References(
                linearConicSectionFromReference(refL).toCrisp(),
                circularConicSectionFromReference(refC).toCrisp(),
                ellipticConicSectionFromReference(refE).toCrisp())
        return ClassifyResult(grades, refs)
    }
}