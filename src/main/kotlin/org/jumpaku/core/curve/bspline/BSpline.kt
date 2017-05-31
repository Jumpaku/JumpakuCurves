package org.jumpaku.curve.bspline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.apache.commons.math3.util.Precision
import org.jumpaku.affine.Divisible
import org.jumpaku.affine.Point
import org.jumpaku.affine.PointJson
import org.jumpaku.affine.Vector
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.FuzzyCurve
import org.jumpaku.curve.Interval
import org.jumpaku.curve.Knot
import org.jumpaku.curve.KnotJson
import org.jumpaku.curve.bezier.Bezier
import org.jumpaku.curve.polyline.Polyline
import org.jumpaku.json.prettyGson
import org.jumpaku.util.*


class BSpline(val controlPoints: Array<Point>, val knots: Array<Knot>) : FuzzyCurve, Differentiable {


    override val domain: Interval = Interval(knots.head().value, knots.last().value)

    override val derivative: BSplineDerivative get() {
        val d = degree
        val knots = knots.zipWithIndex { knot, i -> if (i == 0 || i == knots.size() - 1){
                knot.reduceMultiplicity()
            } else{
                knot
            }
        }
        val ts = knotValues
        val cvs = controlPoints
                .zipWith(controlPoints.tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .zipWithIndex({ v, i -> v*(d / (ts[d + i + 1] - ts[i + 1])) })

        return BSplineDerivative(cvs, knots)
    }

    val knotValues: Array<Double> = knots.flatMap { it.toArray() }

    val degree: Int = knotValues.size() - controlPoints.size() - 1

    init {
        if (knots.zip(knots.tail()).exists({ (a, b) -> a.value > b.value }))
            throw IllegalArgumentException("knots is not ordered in acceding")

        if (!(knots.head().multiplicity == degree + 1 && knots.last().multiplicity == degree + 1))
            throw IllegalArgumentException("knots is not clamped (degree($degree))")

        if (controlPoints.size() <= degree)
            throw IllegalArgumentException("controlPoints.size()(${controlPoints.size()}) <= degree($degree)")
    }

    constructor(controlPoints: Iterable<Point>, knots: Iterable<Knot>) : this(Array.ofAll(controlPoints), Array.ofAll(knots))

    override fun evaluate(t: Double): Point {
        if (t !in domain)
            throw IllegalArgumentException("t($t) is out of domain($domain)")

        if (Precision.equals(t, domain.begin, 1.0e-10)) {
            return controlPoints.head()
        }
        if (Precision.equals(t, domain.end, 1.0e-10)) {
            return controlPoints.last()
        }

        val ts = knotValues
        val l = ts.lastIndexWhere { it <= t }
        val result = controlPoints.toJavaArray(Point::class.java)
        val d = degree

        for (k in 1..d) {
            for (i in l downTo (l - d + k)) {
                val aki = (t - ts[i]) / (ts[i + d + 1 - k] - ts[i])
                result[i] = result[i - 1].divide(aki, result[i])
            }
        }

        return result[l]
    }

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun sampleArcLength(n: Int): Array<Point> = Polyline.approximate(this).sampleArcLength(n)

    fun restrict(begin: Double, end: Double): BSpline {
        if (Interval(begin, end) !in domain) {
            throw IllegalArgumentException("Interval([$begin, $end]) is out of domain($domain)")
        }

        return subdivide(end)._1().subdivide(begin)._2()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline {
        val ks = knots.reverse()
                .map { (v, m) -> Knot(knots.last().value - v + knots.head().value, m) }

        return BSpline(controlPoints.reverse(), ks)
    }

    override fun toString(): String = BSplineJson.toJson(this)

    fun toBeziers(): Array<Bezier> {
        var heads = List<Bezier>()
        var tail = this
        for ((knotValue, _) in knots.tail().init()){
            val (h, t) = tail.subdivide(knotValue)
            heads = heads.prepend(Bezier(h.controlPoints))
            tail = t
        }
        return heads.prepend(Bezier(tail.controlPoints)).reverse().toArray()
    }

    fun subdivide(t: Double): Tuple2<BSpline, BSpline> {
        val i = knotValues.lastIndexWhere { Precision.equals(it, t, 1.0e-10) }
        val u = if (i < 0) t else knotValues[i]

        val inserted = insertKnot(u, degree + 1)

        val firstKnots =  inserted.knots.takeWhile { it.value <= u }
        val firstControlPoints = inserted.controlPoints.take(firstKnots.flatMap(Knot::toArray).size() - degree - 1)
        val secondKnots =  inserted.knots.dropWhile { it.value < u }
        val secondControlPoints = inserted.controlPoints.drop(firstKnots.flatMap(Knot::toArray).size() - degree - 1)

        return Tuple(BSpline(firstControlPoints, firstKnots), BSpline(secondControlPoints, secondKnots))
    }

    fun insertKnot(knotValue: Double, insertionTimes: Int = 1): BSpline {

        if (insertionTimes < 0) {
            throw IllegalArgumentException("negative insertionTimes($insertionTimes)")
        }
        if (knotValue !in domain) {
            throw IllegalArgumentException("inserted knotValue($knotValue) is out of domain($domain).")
        }

        val index = knotValues.indexWhere { Precision.equals(it, knotValue, 1.0e-10) }
        val multiplicity = when{
            index < 0 -> 0
            else -> knots.flatMap { (_, m) -> Stream.fill(m, { m }) }[index]
        }
        val clampedInsertionTimes = minOf(degree - multiplicity + 1, maxOf(0, insertionTimes))

        assert(multiplicity in 0..degree, { "multiplicity($multiplicity) is out of 0..degree($degree)" })
        assert(clampedInsertionTimes in 0..degree - multiplicity+1,
                { "clampedInsertionTimes($clampedInsertionTimes) is out of 0..(degree($degree) - multiplicity($multiplicity))"})

        val cp = createKnotInsertedControlPoints(knotValue, clampedInsertionTimes, multiplicity, knotValues, controlPoints)
        val ks = createInsertedKnots(knotValue, clampedInsertionTimes, multiplicity, knots)

        return BSpline(cp, ks)
    }

    fun insertKnot(i: Int, insertionTimes: Int = 1): BSpline = insertKnot(knotValues[i], insertionTimes)

    companion object {

        internal fun createInsertedKnots(
                knotValue: Double, insertionTimes: Int, multiplicity: Int, knots: Array<Knot>): Array<Knot> {
            val index = knots.search(Knot(knotValue), Comparator.comparingDouble(Knot::value))
            return when (multiplicity) {
                0 -> knots.insert(-index - 1, Knot(knotValue, insertionTimes))
                else -> knots.update(index, { it.elevateMultiplicity(insertionTimes) })
            }
        }

        internal fun <P : Divisible<P>> createKnotInsertedControlPoints(
                knotValue: Double, insertionTimes: Int, multiplicity: Int, us: Array<Double>, cp: Array<P>): Array<P> {
            val p = us.size() - cp.size() - 1
            val k = us.lastIndexWhere { it <= knotValue }

            var front = cp.take(k - p)
            var back = cp.drop(k - multiplicity + 1)
            var middle = cp.subSequence(k - p, k - multiplicity + 1)
            for (r in 1..insertionTimes){
                front = front.append(middle.head())
                back = back.prepend(middle.last())
                middle = middle.zip(middle.tail()).zipWith(Stream.rangeClosed(k - p + r, k - multiplicity),
                        { (prev, next), i -> prev.divide((knotValue - us[i])/(us[i+p]-us[i]), next) })
            }

            return Stream.concat(front, middle, back).toArray()
        }

        fun basis(t: Double, degree: Int, i: Int, us: Array<Double>): Double {
            if (t !in Interval(us.get(degree), us[us.size() - degree - 1])) {
                throw IllegalArgumentException("inserted knot($t) is out of domain([${us[degree]}, ${us[us.size() - 1 - degree]}]).")
            }
            if (Precision.equals(t, us.get(degree), 1.0e-10)) {
                return if (i == 0) 1.0 else 0.0
            }
            if (Precision.equals(t, us[us.size() - degree - 1], 1.0e-10)) {
                return if (i == us.size() - degree - 2) 1.0 else 0.0
            }

            val l = us.lastIndexWhere { it <= t }
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
            return if (java.lang.Double.isFinite((a - b) / (c - d))) {
                (a - b) / (c - d)
            }
            else {
                0.0
            }
        }
    }
}

class BSplineJson(controlPoints: Array<Point>, knots: Array<Knot>){

    private val controlPoints: kotlin.Array<PointJson> = controlPoints.map { PointJson(it.x, it.y, it.z, it.r) }
            .toJavaArray(PointJson::class.java)

    private val knots: kotlin.Array<KnotJson> = knots.map { KnotJson(it.value, it.multiplicity) }
            .toJavaArray(KnotJson::class.java)

    fun bSpline(): BSpline = BSpline(controlPoints.map(PointJson::point), knots.map(KnotJson::knot))

    companion object{

        fun toJson(s: BSpline): String = prettyGson.toJson(BSplineJson(s.controlPoints, s.knots))

        fun fromJson(json: String): Option<BSpline>{
            return try {
                Option(prettyGson.fromJson<BSplineJson>(json).bSpline())
            }
            catch (e: Exception){
                None()
            }
        }
    }
}