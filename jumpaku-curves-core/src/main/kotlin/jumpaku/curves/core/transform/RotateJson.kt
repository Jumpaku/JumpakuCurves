package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.VectorJson

object RotateJson : JsonConverterBase<Rotate>() {

    override fun toJson(src: Rotate): JsonElement = src.run {
        jsonObject(
                "axis" to VectorJson.toJson(axis),
                "angleRadian" to angleRadian.toJson())
    }

    override fun fromJson(json: JsonElement): Rotate =
            Rotate(VectorJson.fromJson(json["axis"]), json["angleRadian"].double)
}