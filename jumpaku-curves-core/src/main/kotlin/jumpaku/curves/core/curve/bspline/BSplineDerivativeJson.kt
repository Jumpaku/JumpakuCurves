package jumpaku.curves.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.VectorJson

object BSplineDerivativeJson : JsonConverterBase<BSplineDerivative>() {

    override fun toJson(src: BSplineDerivative): JsonElement = src.run {
        jsonObject(
            "controlPoints" to jsonArray(controlVectors.map { VectorJson.toJson(it) }),
            "degree" to degree,
            "knots" to jsonArray(knotVector)
        )
    }

    override fun fromJson(json: JsonElement): BSplineDerivative {
        val d = json["degree"].int
        val cp = json["controlPoints"].array.map { VectorJson.fromJson(it) }
        val ks = json["knots"].array.map { it.double }
        return BSplineDerivative(cp, KnotVector(d, ks))
    }
}