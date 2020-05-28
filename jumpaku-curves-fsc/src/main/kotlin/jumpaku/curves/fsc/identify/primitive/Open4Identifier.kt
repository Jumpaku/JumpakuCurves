package jumpaku.curves.fsc.identify.primitive

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator
import jumpaku.curves.fsc.identify.primitive.reference.LinearGenerator


class Open4Identifier(val nSamples: Int = 25, override val nFmps: Int = 15) : Identifier {

    override fun <C : Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult {
        val refL = LinearGenerator().generateBeginEnd(fsc)
        val refC = CircularGenerator(nSamples).generateBeginEnd(fsc)
        val refE = EllipticGenerator(nSamples).generateBeginEnd(fsc)
        val (pL, pC, pE) = listOf(refL, refC, refE).map { fsc.isPossible(it) }
        val grades = hashMapOf(
                CurveClass.LineSegment to (pL),
                CurveClass.CircularArc to (!pL and pC),
                CurveClass.EllipticArc to (!pL and !pC and pE),
                CurveClass.OpenFreeCurve to (!pL and !pC and !pE))
        return IdentifyResult(grades, refL, refC, refE)
    }
}

