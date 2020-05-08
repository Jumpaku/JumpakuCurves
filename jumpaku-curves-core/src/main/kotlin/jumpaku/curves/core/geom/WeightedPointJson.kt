package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object WeightedPointJson : JsonConverterBase<WeightedPoint>() {

    override fun toJson(src: WeightedPoint): JsonElement = src.run {
        jsonObject("point" to PointJson.toJson(point), "weight" to weight.toJson())
    }

    override fun fromJson(json: JsonElement): WeightedPoint =
            WeightedPoint(PointJson.fromJson(json["point"]), json["weight"].double)
}