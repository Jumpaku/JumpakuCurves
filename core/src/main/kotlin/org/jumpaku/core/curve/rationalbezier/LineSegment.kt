package org.jumpaku.core.curve.rationalbezier

import io.vavr.API
import io.vavr.collection.Array
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.ParamPointJson
import org.jumpaku.core.json.prettyGson


/**
 * LineSegment defined by 2 representation points.
 * l(t) = { r0(t1 - t) + r1(t - t0) } / (t1 - t0)
 * 0 <= t0 < t1 <= 1
 */
class LineSegment(val front: ParamPoint, val back: ParamPoint) : FuzzyCurve, Differentiable, Transformable {

    override val domain: Interval = Interval.ZERO_ONE

    init {
        require(front.param < back.param) { "front param(${front.param}) >= back.param(${back.param})" }
        require(Interval(front.param, back.param) in domain) { "front param(${front.param}) or back.param(${back.param}) are out of demain($domain)" }
    }

    val representPoints: Array<Point> get() = API.Array(front.point, back.point)

    val degree = 1

    val asCrispRationalBezier: RationalBezier get() = RationalBezier(
            API.Stream(evaluate(0.0), evaluate(1.0)).map { WeightedPoint(it.toCrisp(), 1.0) })

    override fun toArcLengthCurve(): ArcLengthAdapter = ArcLengthAdapter(this, API.Array(0.0, front.param, back.param, 1.0))

    override val derivative: Derivative get() = asCrispRationalBezier.derivative

    override fun differentiate(t: Double): Vector = asCrispRationalBezier.differentiate(t)

    override fun transform(a: Affine): LineSegment = LineSegment(
            front.copy(point = a(front.point)), back.copy(point = a(back.point)))

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val r = (t - front.param)/(back.param - front.param)
        return front.point.divide(r, back.point)
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): LineSegmentJson = LineSegmentJson(this)

    fun reverse(): LineSegment = LineSegment(back.copy(param = 1.0 - back.param), front.copy(param = 1.0 - front.param))
}

data class LineSegmentJson(val front: ParamPointJson, val back: ParamPointJson){

    constructor(lineSegment: LineSegment) : this(lineSegment.front.json(), lineSegment.back.json())

    fun lineSegment(): LineSegment = LineSegment(front.paramPoint(), back.paramPoint())
}
