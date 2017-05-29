package org.jumpaku.curve.rationalbezier

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.jumpaku.affine.*
import org.jumpaku.curve.Derivative
import org.jumpaku.curve.bezier.BezierDerivative
import org.jumpaku.curve.bezier.Bezier
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.FuzzyCurve
import org.jumpaku.curve.Interval
import org.jumpaku.curve.polyline.Polyline
import org.jumpaku.json.prettyGson


class RationalBezier(val controlPoints: Array<Point>, val weights: Array<Double>) : FuzzyCurve, Differentiable {

    init {
        if (controlPoints.isEmpty) {
            throw IllegalArgumentException("empty controlPoints")
        }
        if (weights.isEmpty) {
            throw IllegalArgumentException("empty weights")
        }
        if (controlPoints.size() != weights.size()) {
            throw IllegalArgumentException("controlPoints.size() != weights.size()")
        }
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
        val dp = BezierDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                if (t !in domain) {
                    throw IllegalArgumentException("t($t) is out of domain($domain)")
                }

                val wt = bezier1D(t, ws)
                val dwt = bezier1D(t, dws)
                val dpt = dp.evaluate(t)
                val rt = this@RationalBezier.evaluate(t).toVector()

                return (1 / wt) * (dpt - dwt * rt)
            }

            override val domain: Interval get() = Interval.ZERO_ONE
        }
    }

    override fun evaluate(t: Double): Point {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain($domain)")
        }

        var wcp = weightedControlPoints
        while (wcp.size() > 1) {
            wcp = Bezier.decasteljau(t, wcp)
        }
        return wcp.head().point
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun toString(): String = RationalBezierJson.toJson(this)

    override fun sampleArcLength(n: Int): Array<Point> = Polyline.approximate(this).sampleArcLength(n)

    fun restrict(i: Interval): RationalBezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): RationalBezier {
        if (Interval(begin, end) !in domain) {
            throw IllegalArgumentException("Interval($begin, $end) is out of domain($domain)")
        }

        return subdivide(end)._1().subdivide(begin / end)._2()
    }


    fun reverse(): RationalBezier = RationalBezier(weightedControlPoints.reverse())

    fun elevate(): RationalBezier = RationalBezier(Bezier.createElevatedControlPoints(weightedControlPoints))

    fun reduce(): RationalBezier {
        if (degree < 1) {
            throw IllegalStateException("degree($degree) is too small")
        }

        return RationalBezier(Bezier.createReducedControlPoints(weightedControlPoints))
    }

    fun subdivide(t: Double): Tuple2<RationalBezier, RationalBezier> {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain()$domain")
        }

        return Bezier.createSubdividedControlPointsArrays(t, weightedControlPoints)
                .map(::RationalBezier, ::RationalBezier)
    }

    companion object {

        fun bezier1D(t: Double, weights: Array<Double>): Double {
            var ws = weights
            while (ws.size() > 1) {
                ws = ws.zipWith(ws.tail()) { w0, w1 -> (1 - t) * w0 + t * w1 }
            }
            return ws.head()
        }

        fun fromBezier(bezier: Bezier): RationalBezier = RationalBezier(bezier.controlPoints, Stream.fill(bezier.degree + 1, { 1.0 }).toArray())
    }
}

class RationalBezierJson(weightedControlPoints: Array<WeightedPoint>){

    private val weightedControlPoints: kotlin.Array<WeightedPointJson> = weightedControlPoints
            .map { (p, w) -> WeightedPointJson(PointJson(p.x, p.y, p.z, p.r), w) }
            .toJavaArray(WeightedPointJson::class.java)

    fun rationalBezier(): RationalBezier = RationalBezier(Array(*weightedControlPoints)
            .map(WeightedPointJson::weightedPoint))

    companion object{

        fun toJson(bezier: RationalBezier): String = prettyGson.toJson(RationalBezierJson(bezier.weightedControlPoints))

        fun fromJson(json: String): Option<RationalBezier> {
            return try {
                Option(prettyGson.fromJson<RationalBezierJson>(json).rationalBezier())
            }
            catch (e: Exception){
                None()
            }
        }
    }
}
