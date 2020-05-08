package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object VectorJson : JsonConverterBase<Vector>() {

    override fun toJson(src: Vector): JsonElement = src.run {
        jsonObject("x" to x, "y" to y, "z" to z)
    }

    override fun fromJson(json: JsonElement): Vector = Vector(json["x"].double, json["y"].double, json["z"].double)

}