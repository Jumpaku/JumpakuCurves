package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson

object ConicSectionJson : JsonConverterBase<ConicSection>() {

    override fun toJson(src: ConicSection): JsonElement = src.run {
        jsonObject(
                "begin" to PointJson.toJson(begin),
                "far" to PointJson.toJson(far),
                "end" to PointJson.toJson(end),
                "weight" to weight.toJson())
    }

    override fun fromJson(json: JsonElement): ConicSection =
            ConicSection(
                    PointJson.fromJson(json["begin"]),
                    PointJson.fromJson(json["far"]),
                    PointJson.fromJson(json["end"]),
                    json["weight"].double)
}