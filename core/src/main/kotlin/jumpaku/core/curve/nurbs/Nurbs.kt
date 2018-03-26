package jumpaku.core.curve.nurbs

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.API
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.arclength.repeatBisection
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.BSplineDerivative
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.apache.commons.math3.util.Precision

class Nurbs(val controlPoints: Array<Point>, val weights: Array<Double>, val knotVector: KnotVector)
    : FuzzyCurve, Differentiable, Transformable, Subdividible<Nurbs>, ToJson {

    val degree: Int = knotVector.degree

    val weightedControlPoints: Array<WeightedPoint> get() = controlPoints.zipWith(weights, ::WeightedPoint)

    override val domain: Interval = knotVector.domain

    override val derivative: Derivative get() {
        val ws = weights
        val dws = ws.zipWith(ws.tail()) { a, b -> degree * (b - a) }
        val dp = BSplineDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }, knotVector).derivative

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = BSpline(weights.map { Point.x(it) }, knotVector).evaluate(t).x
                val dwt = BSpline(dws.map { Point.x(it) }, knotVector.derivativeKnotVector()).evaluate(t).x
                val dpt = dp.evaluate(t)
                val rt = this@Nurbs.evaluate(t).toVector()

                return (dpt - dwt * rt) / wt
            }

            override val domain: Interval get() = this@Nurbs.domain
        }
    }

    init {
        require(controlPoints.nonEmpty()) { "empty controlPoints" }
        check(knotVector.size() - degree - 1 == controlPoints.size()) {
            "knotVector.size()(${knotVector.size()}) - degree($degree) - 1 != controlPoints.size()(${controlPoints.size()})" }
        check(degree > 0) { "degree($degree) <= 0" }
    }

    constructor(weightedControlPoints: Iterable<WeightedPoint>, knotVector: KnotVector) : this(
            Array.ofAll(weightedControlPoints.map { it.point }), Array.ofAll(weightedControlPoints.map { it.weight }), knotVector)

    override fun evaluate(t: Double): Point {
        val l = knotVector.lastIndexUnder(t)
        if(l == knotVector.size() - 1){
            return controlPoints.last()
        }

        val result = weightedControlPoints.toJavaArray(WeightedPoint::class.java)
        val d = degree

        for (k in 1..d) {
            for (i in l downTo (l - d + k)) {
                val aki = BSpline.basisHelper(t, knotVector[i], knotVector[i + d + 1 - k], knotVector[i])
                result[i] = result[i - 1].divide(aki, result[i])
            }
        }

        return result[l].point
    }

    override fun transform(a: Affine): Nurbs = Nurbs(controlPoints.map(a), weights, knotVector)

    override fun toCrisp(): Nurbs = Nurbs(controlPoints.map { it.toCrisp() }, weights, knotVector)

    fun restrict(begin: Double, end: Double): Nurbs {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().subdivide(end)._1()
    }

    fun restrict(i: Interval): Nurbs = restrict(i.begin, i.end)

    fun reverse(): Nurbs = Nurbs(weightedControlPoints.reverse(), knotVector.reverse())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "weightedControlPoints" to jsonArray(weightedControlPoints.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

    /**
     * Multiplies more than degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): Nurbs = restrict(domain)

    fun close(): Nurbs {
        val clamped = clamp()
        val frontCount = clamped.knotVector.filter { it <= domain.begin } .size - degree - 1
        val backCount = clamped.knotVector.filter { it >= domain.end } .size - degree - 1

        val cp = clamped.weightedControlPoints
                .drop(frontCount)
                .dropRight(backCount)
        val closingPoint = cp.head().middle(cp.last())
        val newCp = cp
                .update(0, closingPoint)
                .update(cp.size() - 1, closingPoint)
        val us = clamped.knotVector.knots
                .drop(frontCount)
                .dropRight(backCount)
        val newKnots = KnotVector(degree, us)
        return Nurbs(newCp, newKnots)
    }

    override fun reparametrizeArcLength(): ArcLengthReparametrized {
        val ts = repeatBisection(this, this.domain, { nurbs, subDomain ->
            val wcp = nurbs.restrict(subDomain).weightedControlPoints
            val polylineLength = Polyline(wcp.map { it.point }).reparametrizeArcLength().arcLength()
            val beginEndLength = wcp.head().point.dist(wcp.last().point)
            !(wcp.all { it.weight > 0 } && Precision.equals(polylineLength, beginEndLength, 1.0 / 256))
        }).fold(API.Stream(domain.begin), { acc, subDomain -> acc.append(subDomain.end) })

        return ArcLengthReparametrized(this, ts.toArray())
    }

    fun toRationalBeziers(): Array<RationalBezier> = knotVector.knots
            .slice(degree + 1, knotVector.size() - degree - 1)
            .fold(this, { bSpline, knot -> bSpline.insertKnot(knot, degree) })
            .weightedControlPoints
            .grouped(degree + 1)
            .map(::RationalBezier)
            .toArray()

    override fun subdivide(t: Double): Tuple2<Nurbs, Nurbs> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val (front, back) = BSpline.createSubdividedControlPointsAndKnotVectors(t, weightedControlPoints, knotVector)
        return Tuple2(Nurbs(front._1(), front._2()), Nurbs(back._1(), back._2()))
    }

    fun insertKnot(t: Double, maxMultiplicity: Int = 1): Nurbs {
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(maxMultiplicity >= 0) { "maxMultiplicity($maxMultiplicity) is negative" }

        val (cp, knot) = BSpline.createKnotInsertedControlPointsAndKnotVectors(t, maxMultiplicity, weightedControlPoints, knotVector)

        return Nurbs(cp, knot)
    }

    companion object {

        fun fromJson(json: JsonElement): Option<Nurbs> = Try.ofSupplier {
            Nurbs(json["weightedControlPoints"].array.flatMap { WeightedPoint.fromJson(it) }, KnotVector.fromJson(json["knotVector"]).get())
        }.toOption()
    }
}
