package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object PointJson : JsonConverterBase<Point>() {

    override fun toJson(src: Point): JsonElement = src.run {
        jsonObject("x" to x, "y" to y, "z" to z, "r" to r)
    }

    override fun fromJson(json: JsonElement): Point = Point(json["x"].double, json["y"].double, json["z"].double, json["r"].double)
}