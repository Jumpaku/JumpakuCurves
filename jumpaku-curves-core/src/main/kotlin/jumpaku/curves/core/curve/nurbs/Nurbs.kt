package jumpaku.curves.core.curve.nurbs

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.commons.control.Option
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineDerivative
import jumpaku.curves.core.curve.rationalbezier.RationalBezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2

class Nurbs(
        controlPoints: Iterable<Point>,
        weights: Iterable<Double>,
        val knotVector: KnotVector) : Curve, Differentiable, ToJson {

    constructor(weightedControlPoints: Iterable<WeightedPoint>, knotVector: KnotVector) : this(
            weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight }, knotVector)

    val controlPoints: List<Point> = controlPoints.toList()

    val weights: List<Double> = weights.toList()

    val degree: Int = knotVector.degree

    val weightedControlPoints: List<WeightedPoint> get() = controlPoints.zip(weights, ::WeightedPoint)

    override val domain: Interval = knotVector.domain

    override val derivative: Derivative get() {
        val ws = weights
        val dws = ws.zipWithNext { a, b -> degree * (b - a) }
        val dp = BSplineDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }, knotVector).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = BSpline(weights.map { Point.x(it) }, knotVector).evaluate(t).x
                val dwt = BSpline(dws.map { Point.x(it) }, knotVector.derivativeKnotVector()).evaluate(t).x
                val dpt = dp.evaluate(t)
                val rt = this@Nurbs.evaluate(t).toVector()

                return ((dpt - dwt * rt) / wt).orThrow()
            }

            override val domain: Interval get() = this@Nurbs.domain
        }
    }

    init {
        val us = knotVector.extractedKnots
        val p = knotVector.degree
        val n = this.controlPoints.size
        val m = us.size
        require(n >= p + 1) { "controlPoints.size()($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size()($m) - degree($p) - 1 != controlPoints.size()($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "weightedControlPoints" to jsonArray(weightedControlPoints.map { it.toJson() }),
            "degree" to degree,
            "knots" to jsonArray(knotVector.knots.map { it.toJson() }))

    override fun toCrisp(): Nurbs = Nurbs(controlPoints.map { it.toCrisp() }, weights, knotVector)

    override fun evaluate(t: Double): Point = BSpline.evaluate(weightedControlPoints, knotVector, t).point

    fun transform(a: Transform): Nurbs = Nurbs(controlPoints.map(a::invoke), weights, knotVector)

    fun restrict(begin: Double, end: Double): Nurbs {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().orThrow().subdivide(end)._1().orThrow()
    }

    fun restrict(i: Interval): Nurbs = restrict(i.begin, i.end)

    fun reverse(): Nurbs = Nurbs(weightedControlPoints.reversed(), knotVector.reverse())

    /**
     * Multiplies more than degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): Nurbs = Nurbs(BSpline.clampedControlPoints(weightedControlPoints, knotVector), knotVector.clamp())

    fun close(): Nurbs = Nurbs(BSpline.closedControlPoints(weightedControlPoints, knotVector), knotVector.clamp())

    fun toRationalBeziers(): List<RationalBezier> {
        val (b, e) = domain
        val sb = knotVector.multiplicityOf(b)
        val se = knotVector.multiplicityOf(e)
        return BSpline.segmentedControlPoints(weightedControlPoints, knotVector)
                //.asVavr()
                .run { slice((degree + 1 - sb) until (size - degree - 1 + se)) }
                .windowed(degree + 1, degree + 1)
                //.sliding(degree + 1, degree + 1)
                .map { RationalBezier(it) }
                //.toArray().asKt()
    }

    fun subdivide(t: Double): Tuple2<Option<Nurbs>, Option<Nurbs>> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val (cp0, cp1) = BSpline.subdividedControlPoints(t, weightedControlPoints, knotVector)
        val (kv0, kv1) = knotVector.subdivide(t)
        return Tuple2(kv0.map { Nurbs(cp0, it) }, kv1.map { Nurbs(cp1, it) })
    }

    fun insertKnot(t: Double, times: Int = 1): Nurbs {
        require(t in domain) { "t($t) is out of domain($domain)." }
        val s = knotVector.multiplicityOf(t)
        val p = knotVector.degree
        val h = times.coerceIn(0..(p + 1 - s))
        return Nurbs(BSpline.insertedControlPoints(weightedControlPoints, knotVector, t, h), knotVector.insert(t, h))
    }

    fun removeKnot(knotIndex: Int, times: Int = 1): Nurbs {
        val s = knotVector.knots[knotIndex].multiplicity
        val h = times.coerceIn(0..s)
        return Nurbs(BSpline.removedControlPoints(weightedControlPoints, knotVector, knotIndex, h), knotVector.remove(knotIndex, h))
    }

    companion object {

        fun fromJson(json: JsonElement): Nurbs {
            val d = json["degree"].int
            val wcp = json["weightedControlPoints"].array.map { WeightedPoint.fromJson(it) }
            val ks = json["knots"].array.map { Knot.fromJson(it) }
            return Nurbs(wcp, KnotVector(d, ks))
        }
    }
}
