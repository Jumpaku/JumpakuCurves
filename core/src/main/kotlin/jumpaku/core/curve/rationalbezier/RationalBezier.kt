package jumpaku.core.curve.rationalbezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Derivative
import jumpaku.core.curve.Differentiable
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.BezierDerivative
import jumpaku.core.geom.*
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.util.Result
import jumpaku.core.util.asVavr
import jumpaku.core.util.result


class RationalBezier private constructor(val controlPoints: List<Point>, val weights: List<Double>) : Curve, Differentiable, ToJson {

    init {
        require(controlPoints.isNotEmpty()) { "empty controlPoints" }
        require(weights.isNotEmpty()) { "empty weights" }
        require(controlPoints.size == weights.size) { "controlPoints.size() != weights.size()" }
    }

    constructor(controlPoints: Iterable<Point>, weights: Iterable<Double>) :
            this(controlPoints.toList(), weights.toList())

    constructor(weightedControlPoints: Iterable<WeightedPoint>) :
            this(weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight })

    constructor(vararg weightedControlPoints: WeightedPoint) : this(weightedControlPoints.asIterable())

    val weightedControlPoints: List<WeightedPoint> get() = controlPoints.zip(weights, ::WeightedPoint)

    val degree: Int get() = weightedControlPoints.size - 1

    override val domain: Interval get() = Interval.ZERO_ONE

    override val derivative: Derivative get() {
        val ws = weights
        val dws = ws.zip(ws.asVavr().tail()) { a, b -> degree * (b - a) }
        val dp = BezierDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = bezier1D(t, ws)
                val dwt = bezier1D(t, dws)
                val dpt = dp.evaluate(t)
                val rt = this@RationalBezier.evaluate(t).toVector()

                return (dpt - dwt * rt) / wt
            }

            override val domain: Interval get() = Interval.ZERO_ONE
        }
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return Bezier.createEvaluatedPoint(t, weightedControlPoints).point
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "weightedControlPoints" to jsonArray(weightedControlPoints.map { it.toJson() }))

    fun transform(a: Transform): RationalBezier = RationalBezier(
            weightedControlPoints.map { it.copy(point = a(it.point)) })

    override fun toCrisp(): RationalBezier = RationalBezier(controlPoints.map { it.toCrisp() }, weights)

    fun restrict(i: Interval): RationalBezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): RationalBezier {
        require(Interval(begin, end) in domain) { "Interval($begin, $end) is out of domain($domain)" }

        return subdivide(end)._1().subdivide(begin / end)._2()
    }

    fun reverse(): RationalBezier = RationalBezier(weightedControlPoints.reversed())

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

        fun bezier1D(t: Double, weights: List<Double>): Double {
            var ws = weights
            while (ws.size > 1) {
                ws = ws.zipWithNext { w0, w1 -> w0.divide(t, w1) }
            }
            return ws.first()
        }

        fun fromJson(json: JsonElement): Result<RationalBezier> = result {
            RationalBezier(json["weightedControlPoints"].array.flatMap { WeightedPoint.fromJson(it).value() })
        }
    }
}
