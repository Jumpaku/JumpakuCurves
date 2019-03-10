package jumpaku.curves.fsc

import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline

class DrawingStroke(polyline: Polyline) : Curve by polyline, ToJson by polyline {

    constructor(paramPoints: Iterable<ParamPoint>) : this(Polyline(paramPoints))

    val inputData: List<ParamPoint> = polyline.paramPoints

    val beginParam: Double = domain.begin

    val endParam: Double = domain.end

    val paramSpan: Double = domain.span

    fun extend(paramPoint: ParamPoint): DrawingStroke = DrawingStroke(inputData + paramPoint)

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): DrawingStroke = DrawingStroke(Polyline.fromJson(json))
    }
}