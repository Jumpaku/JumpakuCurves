package jumpaku.curves.core.curve.rationalbezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Derivative
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bezier.BezierDerivative
import jumpaku.curves.core.geom.*
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.transform.Transform


class RationalBezier(controlPoints: Iterable<Point>, weights: Iterable<Double>) : Curve, Differentiable, ToJson {

    constructor(weightedControlPoints: Iterable<WeightedPoint>) :
            this(weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight })

    constructor(vararg weightedControlPoints: WeightedPoint) : this(weightedControlPoints.asIterable())

    val controlPoints: List<Point> = controlPoints.toList()

    val weights: List<Double> = weights.toList()

    init {
        require(this.controlPoints.isNotEmpty()) { "empty controlPoints" }
        require(this.weights.isNotEmpty()) { "empty weights" }
        require(this.controlPoints.size == this.weights.size) { "controlPoints.size() != weights.size()" }
    }

    val weightedControlPoints: List<WeightedPoint> = controlPoints.zip(weights, ::WeightedPoint)

    val degree: Int = weightedControlPoints.size - 1

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: Derivative get() {
        val ws = weights
        val dws = ws.zipWithNext { a, b -> degree * (b - a) }
        val dp = BezierDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = bezier1D(t, ws)
                val dwt = bezier1D(t, dws)
                val dpt = dp.evaluate(t)
                val rt = this@RationalBezier.evaluate(t).toVector()

                return ((dpt - dwt * rt) / wt).orThrow()
            }

            override val domain: Interval = Interval.ZERO_ONE
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
                ws = ws.zipWithNext { w0, w1 -> w0.lerp(t, w1) }
            }
            return ws.first()
        }

        fun fromJson(json: JsonElement): RationalBezier =
            RationalBezier(json["weightedControlPoints"].array.flatMap { WeightedPoint.fromJson(it).value() })
    }
}
