package jumpaku.curves.fsc.identify.primitive.reference

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.IntervalJson
import jumpaku.curves.core.curve.bezier.ConicSectionJson

object ReferenceJson : JsonConverterBase<Reference>() {

    override fun toJson(src: Reference): JsonElement = src.run {
        jsonObject("base" to ConicSectionJson.toJson(base), "domain" to IntervalJson.toJson(domain))
    }

    override fun fromJson(json: JsonElement): Reference =
            Reference(ConicSectionJson.fromJson(json["base"]), IntervalJson.fromJson(json["domain"]))
}