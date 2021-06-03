package jumpaku.curves.core.curve.bspline2

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object KnotVectorJson : JsonConverterBase<KnotVector>() {

    override fun toJson(src: KnotVector): JsonElement =
        jsonObject("degree" to src.degree, "knots" to jsonArray(src))

    override fun fromJson(json: JsonElement): KnotVector =
        KnotVector(json["degree"].int, json["knots"].array.map { it.double })
}