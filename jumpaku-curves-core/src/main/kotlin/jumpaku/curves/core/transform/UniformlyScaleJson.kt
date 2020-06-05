package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object UniformlyScaleJson : JsonConverterBase<UniformlyScale>() {

    override fun toJson(src: UniformlyScale): JsonElement = src.run {
        jsonObject("scale" to scale.toJson())
    }

    override fun fromJson(json: JsonElement): UniformlyScale = UniformlyScale(json["scale"].double)
}