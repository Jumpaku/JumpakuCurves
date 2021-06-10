package jumpaku.curves.fsc

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline

class DrawingStroke(polyline: Polyline) : Curve by polyline {

    constructor(paramPoints: List<ParamPoint>) : this(Polyline(paramPoints))

    val inputData: List<ParamPoint> = polyline.paramPoints

    val beginParam: Double = domain.begin

    val endParam: Double = domain.end

    val paramSpan: Double = domain.span

    override fun toString(): String = "DrawingStroke(inputData=$inputData)"

    fun extend(paramPoint: ParamPoint): DrawingStroke = DrawingStroke(inputData + paramPoint)
}

