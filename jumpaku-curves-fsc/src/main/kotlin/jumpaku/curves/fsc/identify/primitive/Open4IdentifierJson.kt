package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object Open4IdentifierJson : JsonConverterBase<Open4Identifier>() {

    override fun toJson(src: Open4Identifier): JsonElement = src.run {
        jsonObject(
                "nSamples" to nSamples.toJson(),
                "nFmps" to nFmps.toJson())
    }

    override fun fromJson(json: JsonElement): Open4Identifier =
            Open4Identifier(json["nSamples"].int, json["nFmps"].int)
}