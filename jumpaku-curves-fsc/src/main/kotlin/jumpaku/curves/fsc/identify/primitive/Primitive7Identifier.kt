package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator
import jumpaku.curves.fsc.identify.primitive.reference.LinearGenerator


class Primitive7Identifier(val nSamples: Int = 25, override val nFmps: Int = 15) : Identifier {

    override fun <C : Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult {
        val refL = LinearGenerator().generateBeginEnd(fsc)
        val refC = CircularGenerator(nSamples).generateScattered(fsc)
        val refE = EllipticGenerator(nSamples).generateScattered(fsc)
        val (pL, pC, pE) = listOf(refL, refC, refE).map { fsc.isPossible(it) }
        val pClosed = isClosed(fsc)
        val grades = hashMapOf(
                CurveClass.LineSegment to (pL),
                CurveClass.Circle to (pClosed and !pL and pC),
                CurveClass.CircularArc to (!pClosed and !pL and pC),
                CurveClass.Ellipse to (pClosed and !pL and !pC and pE),
                CurveClass.EllipticArc to (!pClosed and !pL and !pC and pE),
                CurveClass.ClosedFreeCurve to (pClosed and !pL and !pC and !pE),
                CurveClass.OpenFreeCurve to (!pClosed and !pL and !pC and !pE))
        return IdentifyResult(grades, refL, refC, refE)
    }

    override fun toJson(): JsonElement = jsonObject(
            "nSamples" to nSamples.toJson(),
            "nFmps" to nFmps.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): Primitive7Identifier =
                Primitive7Identifier(json["nSamples"].int, json["nFmps"].int)
    }
}
