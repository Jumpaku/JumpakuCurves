package jumpaku.core.curve.rationalbezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.affine.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.arclength.repeatBisection
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.BezierDerivative
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson
import org.apache.commons.math3.util.Precision


class RationalBezier(val controlPoints: Array<Point>, val weights: Array<Double>) : FuzzyCurve, Differentiable, Transformable, Subdividible<RationalBezier>, ToJson {

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

    override val derivative: Derivative
        get() {
        val ws = weights
        val dws = ws.zipWith(ws.tail()) { a, b -> degree * (b - a) }
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

        var wcp = weightedControlPoints
        while (wcp.size() > 1) {
            wcp = Bezier.decasteljau(t, wcp)
        }
        return wcp.head().point
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "weightedControlPoints" to jsonArray(weightedControlPoints.map { it.toJson() }))

    override fun transform(a: Affine): RationalBezier = RationalBezier(
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

    override fun subdivide(t: Double): Tuple2<RationalBezier, RationalBezier> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return Bezier.createSubdividedControlPoints(t, weightedControlPoints)
                .map(::RationalBezier, ::RationalBezier)
    }

    override fun toArcLengthCurve(): ArcLengthAdapter {
        val ts = repeatBisection(this, this.domain, { rb, subDomain ->
            val sub = rb.restrict(subDomain)
            val cp = sub.controlPoints
            val ws = sub.weights
            val polylineLength = Polyline(cp).toArcLengthCurve().arcLength()
            val beginEndLength = cp.head().dist(cp.last())
            !(ws.all { it >= 0.0 } && Precision.equals(polylineLength, beginEndLength, 1.0 / 128))
        }).fold(Stream(domain.begin), { acc, subDomain -> acc.append(subDomain.end) })

        return ArcLengthAdapter(this, ts.toArray())
    }

    companion object {

        fun bezier1D(t: Double, weights: Array<Double>): Double {
            var ws = weights
            while (ws.size() > 1) {
                ws = ws.zipWith(ws.tail(), { w0, w1 -> w0.divide(t, w1) })
            }
            return ws.head()
        }
    }
}

val JsonElement.rationalBezier: RationalBezier get() = RationalBezier(
        this["weightedControlPoints"].array.map { it.weightedPoint })

