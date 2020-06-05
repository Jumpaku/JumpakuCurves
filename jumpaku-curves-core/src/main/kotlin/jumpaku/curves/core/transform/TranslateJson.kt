package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.VectorJson

object TranslateJson : JsonConverterBase<Translate>() {

    override fun toJson(src: Translate): JsonElement = src.run {
        jsonObject("move" to VectorJson.toJson(move))
    }

    override fun fromJson(json: JsonElement): Translate = Translate(VectorJson.fromJson(json["move"]))
}