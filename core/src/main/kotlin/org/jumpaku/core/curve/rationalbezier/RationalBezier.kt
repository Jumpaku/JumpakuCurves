package org.jumpaku.core.curve.rationalbezier

import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.bezier.BezierDerivative
import org.jumpaku.core.json.prettyGson


class RationalBezier(val controlPoints: Array<Point>, val weights: Array<Double>) : FuzzyCurve, Differentiable, Transformable {

    init {
        require(controlPoints.nonEmpty()) { "empty controlPoints" }
        require(weights.nonEmpty()) { "empty weights" }
        require(controlPoints.size() == weights.size()) { "controlPoints.size() != weights.size()" }
    }

    constructor(weightedControlPoints: Array<WeightedPoint>) : this(
            weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight })

    constructor(weightedControlPoints: Iterable<WeightedPoint>) : this(Array.ofAll(weightedControlPoints))

    constructor(vararg weightedControlPoints: WeightedPoint) : this(Array(*weightedControlPoints))

    val weightedControlPoints: Array<WeightedPoint> get() = controlPoints.zipWith(weights, ::WeightedPoint)

    val degree: Int get() = weightedControlPoints.size() - 1

    override val domain: Interval get() = Interval.ZERO_ONE

    override val derivative: Derivative get() {
        val ws = weights
        val dws = ws.zipWith(ws.tail()) { a, b -> degree * (b - a) }
        val dp = BezierDerivative(weightedControlPoints.map { (p, w) -> p.vector * w }).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = bezier1D(t, ws)
                val dwt = bezier1D(t, dws)
                val dpt = dp.evaluate(t)
                val rt = this@RationalBezier.evaluate(t).vector

                return (1 / wt) * (dpt - dwt * rt)
            }

            override val domain: Interval get() = Interval.ZERO_ONE
        }
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        var wcp = weightedControlPoints
        while (wcp.size() > 1) {
            wcp = Bezier.decasteljau(t, wcp)
        }
        return wcp.head().point
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): RationalBezierJson = RationalBezierJson(this)

    override fun transform(a: Transform): RationalBezier = RationalBezier(
            weightedControlPoints.map { it.copy(point = a(it.point)) })

    fun restrict(i: Interval): RationalBezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): RationalBezier {
        require(Interval(begin, end) in domain) { "Interval($begin, $end) is out of domain($domain)" }

        return subdivide(end)._1().subdivide(begin / end)._2()
    }


    fun reverse(): RationalBezier = RationalBezier(weightedControlPoints.reverse())

    fun elevate(): RationalBezier = RationalBezier(Bezier.createElevatedControlPoints(weightedControlPoints))

    fun reduce(): RationalBezier {
        require(degree >= 1) { "degree($degree) is too small" }

        return RationalBezier(Bezier.createReducedControlPoints(weightedControlPoints))
    }

    fun subdivide(t: Double): Tuple2<RationalBezier, RationalBezier> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return Bezier.createSubdividedControlPoints(t, weightedControlPoints)
                .map(::RationalBezier, ::RationalBezier)
    }

    companion object {

        fun bezier1D(t: Double, weights: Array<Double>): Double {
            var ws = weights
            while (ws.size() > 1) {
                ws = ws.zipWith(ws.tail(), { w0, w1 -> w0.divide(t, w1) })
            }
            return ws.head()
        }

        fun fromBezier(bezier: Bezier): RationalBezier = RationalBezier(bezier.controlPoints.map { WeightedPoint(it) })
    }
}

data class RationalBezierJson(private val weightedControlPoints: List<WeightedPointJson>){

    constructor(rationalBezier: RationalBezier) : this(rationalBezier.weightedControlPoints.map(WeightedPoint::json).toJavaList())

    fun rationalBezier(): RationalBezier = RationalBezier(weightedControlPoints.map(WeightedPointJson::weightedPoint))
}
