package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object Primitive7IdentifierJson : JsonConverterBase<Primitive7Identifier>() {

    override fun toJson(src: Primitive7Identifier): JsonElement = src.run {
        jsonObject(
                "nSamples" to nSamples.toJson(),
                "nFmps" to nFmps.toJson())
    }

    override fun fromJson(json: JsonElement): Primitive7Identifier =
            Primitive7Identifier(json["nSamples"].int, json["nFmps"].int)
}