package jumpaku.curves.core.curve.polyline

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.IntervalJson
import jumpaku.curves.core.geom.PointJson

object LineSegmentJson : JsonConverterBase<LineSegment>() {

    override fun toJson(src: LineSegment): JsonElement = src.run {
        jsonObject(
                "begin" to PointJson.toJson(begin),
                "end" to PointJson.toJson(end),
                "domain" to IntervalJson.toJson(domain))
    }

    override fun fromJson(json: JsonElement): LineSegment = LineSegment(
            PointJson.fromJson(json["begin"]),
            PointJson.fromJson(json["end"]),
            IntervalJson.fromJson(json["domain"]))
}