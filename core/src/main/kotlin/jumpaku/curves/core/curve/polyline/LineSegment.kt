package jumpaku.curves.core.curve.polyline

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.json.ToJson

class LineSegment(val begin: ParamPoint, val end: ParamPoint): Curve by Polyline(listOf(begin, end)), ToJson {

    init {
        require(begin.param <= end.param) { "must be begin.param(${begin.param}) <= end.param(${end.param})" }
    }

    override fun toJson(): JsonElement = jsonObject("begin" to begin.toJson(), "end" to end.toJson())

    companion object {

        fun fromJson(json: JsonElement): LineSegment =
                LineSegment(ParamPoint.fromJson(json["begin"]), ParamPoint.fromJson(json["end"]))
    }
}