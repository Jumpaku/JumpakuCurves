package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.fuzzy.Grade

object OverlapStatePathJson : JsonConverterBase<OverlapState.Path>() {

    override fun fromJson(json: JsonElement): OverlapState.Path = OverlapState.Path(
        Grade(json["grade"].double),
        json["values"].array.map { OverlapMatrix.Key(it["row"].int, it["column"].int) }.toList()
    )

    override fun toJson(src: OverlapState.Path): JsonElement = jsonObject(
        "grade" to src.grade.value,
        "values" to src.map { jsonObject("row" to it.row, "column" to it.column) }.toJsonArray())

}

object OverlapStateJson : JsonConverterBase<OverlapState>() {

    override fun fromJson(json: JsonElement): OverlapState = when (json["type"].string) {
        "NotDetected" -> OverlapState.NotDetected(OverlapMatrixJson.fromJson(json["osm"]))
        "Detected" -> OverlapState.Detected(
            OverlapMatrixJson.fromJson(json["osm"]),
            OverlapStatePathJson.fromJson(json["front"]),
            OverlapStatePathJson.fromJson(json["middle"]),
            OverlapStatePathJson.fromJson(json["back"]),
        )
        else -> error("type(${json["type"]}) must be NotDetected or Detected")
    }

    override fun toJson(src: OverlapState): JsonElement = when (src) {
        is OverlapState.NotDetected -> jsonObject("type" to "NotDetected", "osm" to OverlapMatrixJson.toJson(src.osm))
        is OverlapState.Detected -> jsonObject(
            "type" to "Detected",
            "osm" to OverlapMatrixJson.toJson(src.osm),
            "front" to OverlapStatePathJson.toJson(src.front),
            "middle" to OverlapStatePathJson.toJson(src.middle),
            "back" to OverlapStatePathJson.toJson(src.back),
        )
    }

}