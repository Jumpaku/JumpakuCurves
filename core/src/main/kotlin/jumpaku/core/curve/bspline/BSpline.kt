package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.affine.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.arclength.repeatBisection
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.divOption
import org.apache.commons.math3.util.Precision


class BSpline(val controlPoints: Array<Point>, val knotVector: KnotVector)
    : FuzzyCurve, Differentiable, Transformable, Subdividible<BSpline>, ToJson {

    val degree: Int = knotVector.degree

    override val domain: Interval = knotVector.domain

    override val derivative: BSplineDerivative
        get() {
        val cvs = controlPoints
                .zipWith(controlPoints.tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .zipWithIndex({ v, i ->
                    v* basisHelper(degree.toDouble(), 0.0, knotVector[degree + i + 1], knotVector[i + 1])
                })

        return BSplineDerivative(cvs, knotVector.derivativeKnotVector())
    }


    init {
        require(controlPoints.nonEmpty()) { "empty controlPoints" }
        check(knotVector.size() - degree - 1 == controlPoints.size()) {
            "knotVector.size()(${knotVector.size()}) - degree($degree) - 1 != controlPoints.size()(${controlPoints.size()})" }
        check(degree > 0) { "degree($degree) <= 0" }
    }

    constructor(controlPoints: Iterable<Point>, knots: KnotVector) : this(Array.ofAll(controlPoints), knots)

    override fun evaluate(t: Double): Point {
        val l = knotVector.lastIndexUnder(t)
        if(l == knotVector.size() - 1){
            return controlPoints.last()
        }

        val result = controlPoints.toJavaArray(Point::class.java)
        val d = degree

        for (k in 1..d) {
            for (i in l downTo (l - d + k)) {
                val aki = basisHelper(t, knotVector[i], knotVector[i + d + 1 - k], knotVector[i])
                result[i] = result[i - 1].divide(aki, result[i])
            }
        }

        return result[l]
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun transform(a: Affine): BSpline = BSpline(controlPoints.map(a), knotVector)

    fun restrict(begin: Double, end: Double): BSpline {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().subdivide(end)._1()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(controlPoints.reverse(), knotVector.reverse())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

    override fun toArcLengthCurve(): ArcLengthAdapter {
        val ts = repeatBisection(this, this.domain, { bSpline, subDomain ->
            val cp = bSpline.restrict(subDomain).controlPoints
            val polylineLength = Polyline(cp).toArcLengthCurve().arcLength()
            val beginEndLength = cp.head().dist(cp.last())
            !Precision.equals(polylineLength, beginEndLength, 1.0 / 256)
        }).fold(Stream(domain.begin), { acc, subDomain -> acc.append(subDomain.end) })

        return ArcLengthAdapter(this, ts.toArray())
    }

    /**
     * Multiplies more than degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): BSpline = restrict(domain)//restrict inserts multiple knots into begin and ends

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline {
        val clamped = clamp()
        val frontCount = clamped.knotVector.filter { it <= domain.begin } .size - degree - 1
        val backCount = clamped.knotVector.filter { it >= domain.end } .size - degree - 1

        //insertion algorithm of restrict in clamp multiplies degree + 2 knots at end of domain
        //and last 2 control points are the same point
        val cp = clamped.controlPoints
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
        return BSpline(newCp, newKnots)
    }

    fun toBeziers(): Array<Bezier> {
        return knotVector.knots
                .slice(degree + 1, knotVector.size() - degree - 1)
                .fold(this, { bSpline, knot -> bSpline.insertKnot(knot, degree) })
                .controlPoints
                .grouped(degree + 1)
                .map(::Bezier)
                .toArray()
    }

    override fun subdivide(t: Double): Tuple2<BSpline, BSpline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val (front, back) = createSubdividedControlPointsAndKnotVectors(t, controlPoints, knotVector)
        return Tuple2(BSpline(front._1(), front._2()), BSpline(back._1(), back._2()))
    }

    fun insertKnot(t: Double, maxMultiplicity: Int = 1): BSpline {
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(maxMultiplicity >= 0) { "maxMultiplicity($maxMultiplicity) is negative" }

        val (cp, knot) = createKnotInsertedControlPointsAndKnotVectors(t, maxMultiplicity, controlPoints, knotVector)

        return BSpline(cp, knot)
    }

    companion object {

        internal fun <D : Divisible<D>> createKnotInsertedControlPoints(
                u: Double, controlPoints: Array<D>, knots: KnotVector): Array<D> {
            val p = knots.degree
            val k = knots.lastIndexUnder(u)
            val front = controlPoints.take(k - p + 1)
            val back = controlPoints.drop(k)

            val a = ((k - p + 1)..k).map { basisHelper(u, knots[it], knots[it + p], knots[it]) }
            val middle = controlPoints.slice(k - p, k + 1)
                    .let { it.zip(it.tail()).zipWith(a, { (p0, p1), ai -> p0.divide(ai, p1) }) }

            return Stream.concat(front, middle, back).toArray()
        }

        internal fun <D : Divisible<D>> createKnotInsertedControlPointsAndKnotVectors(
                t: Double, insertionTimes: Int, controlPoints: Array<D>, knots: KnotVector): Tuple2<Array<D>, KnotVector> {
            var insertedCps = controlPoints
            var insertedKnots = knots
            for (i in 1..insertionTimes){
                insertedCps = createKnotInsertedControlPoints(t, insertedCps, insertedKnots)
                insertedKnots = insertedKnots.insertKnot(t)
            }
            return Tuple(insertedCps, insertedKnots)
        }

        internal fun <D : Divisible<D>> createSubdividedControlPointsAndKnotVectors(
                t: Double, controlPoints: Array<D>, knotVector: KnotVector
        ): Tuple2<Tuple2<Array<D>, KnotVector>, Tuple2<Array<D>, KnotVector>> {
            val degree = knotVector.degree
            val (cps, _) = createKnotInsertedControlPointsAndKnotVectors(t, degree + 1, controlPoints, knotVector)
            val (frontKnot, backKnot) = knotVector.subdivide(t)
            val size = frontKnot.size() - degree - 1
            val frontCp = cps.take(size)
            val backCp = cps.drop(size)

            return Tuple(Tuple(frontCp, frontKnot), Tuple(backCp, backKnot))
        }

        fun basis(t: Double, degree: Int, i: Int, us: KnotVector): Double {
            require(t in Interval(us[degree], us[us.size() - degree - 1])) { "knot($t) is out of domain([${us[degree]}, ${us[us.size() - 1 - degree]}])." }

            if (Precision.equals(t, us[degree], 1.0e-10)) {
                return if (i == 0) 1.0 else 0.0
            }
            if (Precision.equals(t, us[us.size() - degree - 1], 1.0e-10)) {
                return if (i == us.size() - degree - 2) 1.0 else 0.0
            }

            val l = us.lastIndexUnder(t)
            val ns = Stream.rangeClosed(0, degree)
                    .map { index -> if ( index == l - i ) 1.0 else 0.0}
                    .toJavaArray(Double::class.java)

            for (j in 1..(ns.size - 1)) {
                for (k in 0..(ns.size - j - 1)) {
                    val left = basisHelper(t, us[k + i], us[k + i + j], us[k + i])
                    val right = basisHelper(us[k + 1 + i + j], t, us[k + 1 + i + j], us[k + 1 + i])
                    ns[k] = left * ns[k] + right * ns[k + 1]
                }
            }

            return ns[0]
        }

        private fun basisHelper(a: Double, b: Double, c: Double, d: Double): Double {
            return (a - b).divOption (c - d).getOrElse(0.0)
        }
    }
}

val JsonElement.bSpline: BSpline get() = BSpline(
        this["controlPoints"].array.map { it.point }, this["knotVector"].knotVector)
