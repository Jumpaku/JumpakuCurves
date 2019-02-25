package jumpaku.fsc

import com.google.gson.JsonElement
import jumpaku.core.curve.Curve
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson

class DrawingStroke(polyline: Polyline): Curve by polyline, ToJson by polyline {

    constructor(paramPoints: Iterable<ParamPoint>): this(Polyline(paramPoints))

    val paramPoints: List<ParamPoint> = polyline.paramPoints

    val beginParam: Double = domain.begin

    val endParam: Double = domain.end

    val paramSpan: Double = domain.span

    fun extend(paramPoint: ParamPoint): DrawingStroke = DrawingStroke(paramPoints + paramPoint)

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): DrawingStroke = DrawingStroke(Polyline.fromJson(json))
    }
}