package org.jumpaku.core.curve.bspline

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


class BSpline(val controlPoints: Array<Point>, val knotVector: KnotVector) : FuzzyCurve, Differentiable, CrispTransformable {

    val degree: Int = knotVector.size() - controlPoints.size() - 1

    override val domain: Interval = knotVector.domain(degree)

    override val derivative: BSplineDerivative get() {
        check(knotVector.innerKnotVector().multiplicity.forAll { it <= degree + 1 }) { "C^0 curve is not differentiable " }

        val cvs = controlPoints
                .zipWith(controlPoints.tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .zipWithIndex({ v, i ->
                    v*(degree / (knotVector[degree + i + 1] - knotVector[i + 1]))
                })

        return BSplineDerivative(cvs, knotVector.innerKnotVector())
    }


    init {
        require(controlPoints.nonEmpty()) { "empty controlPoints" }
        check(knotVector.size() - degree - 1 == controlPoints.size()) {
            "knotVector.size()(${knotVector.size()}) - degree($degree) - 1 != controlPoints.size()(${controlPoints.size()})" }
        check(degree > 0) { "degree($degree) <= 0" }
        //require(controlPoints.size() > degree) { "controlPoints size(${controlPoints.size()}) <= degree($degree)" }
    }

    constructor(controlPoints: Iterable<Point>, knots: KnotVector) : this(Array.ofAll(controlPoints), knots)

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val l = knotVector.lastIndexUnder(t)

        if(l == knotVector.size() - 1){
            return controlPoints.last()
        }

        val result = controlPoints.toJavaArray(Point::class.java)
        val d = degree

        for (k in 1..d) {
            for (i in l downTo (l - d + k)) {
                val aki = (t - knotVector[i]) / (knotVector[i + d + 1 - k] - knotVector[i])
                result[i] = result[i - 1].divide(aki, result[i])
            }
        }

        return result[l]
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun crispTransform(a: Transform): BSpline = BSpline(controlPoints.map { a(it.toCrisp())}, knotVector)

    fun restrict(begin: Double, end: Double): BSpline {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin).last().subdivide(end).head()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(controlPoints.reverse(), knotVector.reverse(degree))

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): BSplineJson = BSplineJson(this)

    fun toBeziers(): Array<Bezier> {
        var insertedControlPoints = controlPoints
        var insertedKnot = knotVector
        for (knot in knotVector.value.slice(degree, knotVector.size() - degree - 1)){
            insertedControlPoints = createKnotInsertedControlPoints(knot, degree + 1, degree, insertedControlPoints, insertedKnot)
            insertedKnot = insertedKnot.insertKnot(degree, knot, degree + 1)
        }
        var beziers = Stream<Bezier>()
        var tail = insertedControlPoints
        while (tail.size() >= degree + 1){
            beziers = beziers.append(Bezier(tail.take(degree + 1)))
            tail = tail.drop(degree + 1)
        }
        return beziers.toArray()
    }

    fun subdivide(t: Double): Array<BSpline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val subdividedKnots = knotVector.subdivide(degree, t)
        val subdividedControlPoints = createSubdividedControlPoints(t, degree, controlPoints, knotVector)
        return subdividedControlPoints.zipWith(subdividedKnots, ::BSpline)
    }

    fun insertKnot(knotValue: Double, maxInsertionTimes: Int = 1): BSpline {
        require(maxInsertionTimes >= 0) { "negative maxInsertionTimes($maxInsertionTimes)" }
        require(knotValue in domain) { "inserted value($knotValue) is out of domain($domain)." }

        val inserted = createKnotInsertedControlPoints(knotValue, maxInsertionTimes, degree, controlPoints, knotVector)

        return BSpline(inserted, knotVector.insertKnot(degree, knotValue, maxInsertionTimes))
    }

    fun insertKnot(i: Int, maxInsertionTimes: Int = 1): BSpline = insertKnot(knotVector[i], maxInsertionTimes)

    companion object {

        internal fun <D : Divisible<D>> createSubdividedControlPoints(
                t: Double, degree: Int, controlPoints: Array<D>, knotVector: KnotVector): Array<Array<D>> {
            val inserted = createKnotInsertedControlPoints(t, degree + 1, degree, controlPoints, knotVector)
            val size = knotVector.subdivide(degree, t).head().size() - degree - 1
            val first = inserted.take(size).run { if (isEmpty){ Array(controlPoints.head()) }else{ this } }
            val second = inserted.drop(size).run { if (isEmpty){ Array(controlPoints.last()) }else{ this } }

            return Array(first, second)
        }

        internal fun <D : Divisible<D>> createKnotInsertedControlPoints(
                knotValue: Double, insertionTimes: Int, degree: Int, controlPoints: Array<D>, knots: KnotVector): Array<D> {

            val m = knots.indexCloseTo(knotValue).map { knots.multiplicity[it] } .getOrElse(0)
            val u = knots.indexCloseTo(knotValue).map { knots[it] } .getOrElse(knotValue)
            val times = knots.clampInsertionTimes(degree, u, insertionTimes)
            val k = knots.lastIndexUnder(u)

            var front = controlPoints.take(k - degree)
            var back = controlPoints.drop(k - m + 1)
            var middle = controlPoints.slice(k - degree, k - m + 1)
            for (r in 1..times){
                front = front.append(middle.head())
                back = back.prepend(middle.last())
                middle = middle.zip(middle.tail()).zipWith((k - degree + r)..(k - m),
                        { (prev, next), i -> prev.divide((u - knots[i])/(knots[i+degree] - knots[i]), next) })
            }

            return Stream.concat(front, middle, back).toArray()
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
            return if (((a - b) / (c - d)).isFinite()) {
                (a - b) / (c - d)
            }
            else {
                0.0
            }
        }
    }
}

data class BSplineJson(val controlPoints: List<PointJson>, val knotVector: KnotVectorJson){

    constructor(bSpline: BSpline) : this(bSpline.controlPoints.map(Point::json).toJavaList(), bSpline.knotVector.json())

    fun bSpline(): BSpline = BSpline(controlPoints.map(PointJson::point), knotVector.knotVector())
}