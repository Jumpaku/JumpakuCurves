package jumpaku.fsc.identify

import jumpaku.core.curve.Curve
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.hashMap
import jumpaku.fsc.identify.reference.CircularGenerator
import jumpaku.fsc.identify.reference.EllipticGenerator
import jumpaku.fsc.identify.reference.LinearGenerator


class Primitive7Identifier(val nSamples: Int = 25, override val nFmps: Int = 15): Identifier {

    fun isClosed(fsc: Curve): Grade = fsc.evaluateAll(2).run { head().isPossible(last()) }

    override fun <C : Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult {
        val refL = LinearGenerator().generateBeginEnd(fsc)
        val refC = CircularGenerator(nSamples).generateScattered(fsc)
        val refE = EllipticGenerator(nSamples).generateScattered(fsc)
        val (pL, pC, pE) = listOf(refL, refC, refE).map { ref -> fsc.isPossible(ref.reparametrized, nFmps) }
        val pClosed = isClosed(fsc)
        val grades = hashMap(
                CurveClass.LineSegment to (pL),
                CurveClass.Circle to (pClosed and !pL and pC),
                CurveClass.CircularArc to (!pClosed and !pL and pC),
                CurveClass.Ellipse to (pClosed and !pL and !pC and pE),
                CurveClass.EllipticArc to (!pClosed and !pL and !pC and pE),
                CurveClass.ClosedFreeCurve to (pClosed and !pL and !pC and !pE),
                CurveClass.OpenFreeCurve to (!pClosed and !pL and !pC and !pE))
        return IdentifyResult(grades, refL, refC, refE)
    }
}