package jumpaku.curves.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.KnotJson
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.geom.VectorJson

object BSplineDerivativeJson : JsonConverterBase<BSplineDerivative>() {

    override fun toJson(src: BSplineDerivative): JsonElement = src.run {
        jsonObject(
                "controlPoints" to jsonArray(controlVectors.map { VectorJson.toJson(it) }),
                "degree" to degree,
                "knots" to jsonArray(knotVector.knots.map { KnotJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): BSplineDerivative {
        val d = json["degree"].int
        val cp = json["controlPoints"].array.map { VectorJson.fromJson(it) }
        val ks = json["knots"].array.map { KnotJson.fromJson(it) }
        return BSplineDerivative(cp, KnotVector(d, ks))
    }
}