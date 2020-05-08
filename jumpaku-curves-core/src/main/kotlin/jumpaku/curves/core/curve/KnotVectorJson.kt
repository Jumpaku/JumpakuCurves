package jumpaku.curves.core.curve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object KnotJson : JsonConverterBase<Knot>() {

    override fun toJson(src: Knot): JsonElement = src.run {
        jsonObject("value" to value, "multiplicity" to multiplicity)
    }

    override fun fromJson(json: JsonElement): Knot = Knot(json["value"].double, json["multiplicity"].int)
}

object KnotVectorJson : JsonConverterBase<KnotVector>() {

    override fun toJson(src: KnotVector): JsonElement = src.run {
        jsonObject("degree" to degree, "knots" to jsonArray(knots.map { KnotJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): KnotVector = KnotVector(json["degree"].int, json["knots"].array.map { KnotJson.fromJson(it) })

}