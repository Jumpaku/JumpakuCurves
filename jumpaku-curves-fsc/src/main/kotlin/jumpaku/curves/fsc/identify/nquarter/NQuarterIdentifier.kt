package jumpaku.curves.fsc.identify.nquarter

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.identify.nquarter.reference.NQuarterCircular
import jumpaku.curves.fsc.identify.nquarter.reference.NQuarterElliptic
import jumpaku.curves.fsc.identify.primitive.reference.Reference

/**
 * Identifies an FSC that has been identified as a circular arc or elliptic arc as one of the four subclasses (1-quarter, 2-quarters, 3-quarters, or general) of sub-geometric primitives.
 * The concept of this process is proposed in the following papers:
 * Kousuke NAKAJIMA, Naoya HATAMOTO, Tomohiko ITO, Takeshi SHIBATA, and Sato SAGA. Identification of n-Quarter Circular Arcs and n-Quarter Elliptic Arcs Based on Freehand Curve Identifier FSCI. Journal of Japan Society for Fuzzy Theory and Intelligent Informatics 2019;31(3):701-711. URL: https://ci.nii.ac.jp/naid/130007663811/en/ DOI: 10.3156/jsoft.31.3_701
 */
class NQuarterIdentifier(val nSamples: Int = 25, val nFmps: Int = 15) {

    private fun <C : Curve> evaluate(
        reference1: Reference,
        reference2: Reference,
        reference3: Reference,
        fsc: ReparametrizedCurve<C>
    ): Map<NQuarterClass, Grade> {
        val (pq1, pq2, pq3) = listOf(reference1, reference2, reference3).map {
            fsc.isPossible(it.reparametrized, nFmps)
        }
        return mapOf(
            NQuarterClass.Quarter1 to pq1,
            NQuarterClass.Quarter2 to pq2,
            NQuarterClass.Quarter3 to pq3,
            NQuarterClass.General to (!pq1 and !pq2 and !pq3)
        )
    }

    fun <C : Curve> identifyCircular(fsc: ReparametrizedCurve<C>): NQuarterIdentifyResult {
        val (q1, q2, q3) = (1..3).map { NQuarterCircular().generate(it, fsc) }
        val grades = evaluate(q1, q2, q3, fsc)
        return NQuarterIdentifyResult(grades, q1, q2, q3)
    }

    fun <C : Curve> identifyElliptic(fsc: ReparametrizedCurve<C>): NQuarterIdentifyResult {
        val (q1, q2, q3) = (1..3).map { NQuarterElliptic(nSamples).generate(it, fsc) }
        val grades = evaluate(q1, q2, q3, fsc)
        return NQuarterIdentifyResult(grades, q1, q2, q3)
    }
}

object NQuarterIdentifierJson : JsonConverterBase<NQuarterIdentifier>() {

    override fun toJson(src: NQuarterIdentifier): JsonElement = src.run {
        jsonObject(
            "nSamples" to nSamples.toJson(),
            "nFmps" to nFmps.toJson()
        )
    }

    override fun fromJson(json: JsonElement): NQuarterIdentifier =
        NQuarterIdentifier(json["nSamples"].int, json["nFmps"].int)
}