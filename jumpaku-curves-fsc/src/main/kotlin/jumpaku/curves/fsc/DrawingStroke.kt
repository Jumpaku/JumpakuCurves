package jumpaku.curves.fsc

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.transform.AffineTransform
import jumpaku.curves.core.transform.SimilarityTransform

class DrawingStroke(polyline: Polyline) : Curve by polyline {

    constructor(paramPoints: List<ParamPoint>) : this(Polyline(paramPoints))

    val inputData: List<ParamPoint> = polyline.paramPoints

    val beginParam: Double = domain.begin

    val endParam: Double = domain.end

    val paramSpan: Double = domain.span

    override fun toString(): String = "DrawingStroke(inputData=$inputData)"

    fun extend(paramPoint: ParamPoint): DrawingStroke = DrawingStroke(inputData + paramPoint)

    override fun affineTransform(a: AffineTransform): DrawingStroke =
        DrawingStroke(inputData.map { it.copy(point = a(it.point)) })

    override fun similarlyTransform(a: SimilarityTransform): DrawingStroke =
        DrawingStroke(inputData.map { it.copy(point = a(it.point)) })
}

