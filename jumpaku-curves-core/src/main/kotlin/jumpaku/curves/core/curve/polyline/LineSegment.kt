package jumpaku.curves.core.curve.polyline

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point


class LineSegment(begin: ParamPoint, end: ParamPoint): Curve by Polyline(listOf(begin, end)), ToJson {

    constructor(begin: Point, end: Point, domain: Interval = Interval.ZERO_ONE):
            this(ParamPoint(begin, domain.begin), ParamPoint(end, domain.end))

    init {
        require(begin.param < end.param) { "must be begin.param(${begin.param}) < end.param(${end.param})" }
    }

    val begin: Point = begin.point

    val end: Point = end.point

    override fun toJson(): JsonElement = jsonObject(
            "begin" to begin.toJson(),
            "end" to end.toJson(),
            "domain" to domain.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): LineSegment = LineSegment(
                Point.fromJson(json["begin"]),
                Point.fromJson(json["end"]),
                Interval.fromJson(json["domain"]))
    }
}