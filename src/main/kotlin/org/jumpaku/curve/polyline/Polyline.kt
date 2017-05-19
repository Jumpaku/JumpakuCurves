package org.jumpaku.curve.polyline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import io.vavr.control.Option
import org.apache.commons.math3.util.Precision
import org.jumpaku.affine.Point
import org.jumpaku.affine.PointJson
import org.jumpaku.affine.times
import org.jumpaku.curve.Curve
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.FuzzyCurve
import org.jumpaku.curve.Interval
import org.jumpaku.json.prettyGson
import org.jumpaku.util.component1
import org.jumpaku.util.component2


class Polyline (val points: Array<Point>, private val parameters: Array<Double>) : FuzzyCurve {

    init {
        if(points.size() != parameters.size()){
            throw IllegalArgumentException("points.size() != parameters.size()")
        }
    }

    override val domain: Interval = Interval(parameters.head(), parameters.last())

    constructor(points: Array<Point>) :this(
            points,
            Stream.iterate(
                    Tuple(0.0, points.zipWith(points.tail(), { p, q -> p.toCrisp().dist(q.toCrisp()) })),
                    { (sum, ds) -> Tuple(sum + ds.head(), ds.tail()) })
                    .map { it._1() }
                    .take(points.size())
                    .toArray())

    constructor(points: Iterable<Point>) : this(Array.ofAll(points))

    constructor(vararg points: Point) : this(Array(*points))

    override fun toString(): String = PolylineJson.toJson(this)

    override fun evaluate(t: Double): Point {
        if (t !in domain) {
            throw IllegalArgumentException("t=$t is out of $domain")
        }

        val i = parameters.search(t)
        return if(i >= 0) {
            points[i]
        }
        else {
            evaluateInSpan(t, -2 - i)
        }
    }

    private fun evaluateInSpan(t: Double, index: Int): Point = points[index].divide(
                (t - parameters[index]) / (parameters[index+1] - parameters[index]), points[index+1])

    override fun sampleArcLength(n: Int): Array<Point> {
        if (points.isSingleValued) {
            return Stream.fill(n,  { points.head() }).toArray()
        }

        val evaluated = ArrayList<Point>(n)
        val ts = domain.sample(n).subSequence(1, n-1)
        var index = 0
        for (t in ts) {
            index = parameters.indexWhere({ t < it }, index)
            evaluated += evaluateInSpan(t, index-1)
        }

        return Stream.ofAll(evaluated).prepend(points.head()).append(points.last()).toArray()
    }

    fun reverse(): Polyline = Polyline(points.reverse(), parameters.map { domain.end + domain.begin - it }.reverse())

    fun restrict(i: Interval): Polyline = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Polyline = subdivide(begin)._2().subdivide(end-begin)._1()

    fun subdivide(t: Double): Tuple2<Polyline, Polyline> {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain($domain)")
        }
        val index = parameters.search(t)
        return when{
            index >= 0 ->
                Tuple(Polyline(points.take(index+1), parameters.take(index+1)),
                        Polyline(points.drop(index), parameters.drop(index).map { it - t }))
            Precision.equals(parameters[-2-index], t, 1.0e-10) ->
                Tuple(Polyline(points.take(-1-index), parameters.take(-1-index)),
                        Polyline(points.drop(-2-index), parameters.drop(-2-index).map { it - t }))
            Precision.equals(parameters[-1-index], t, 1.0e-10) ->
                Tuple(Polyline(points.take(-index), parameters.take(-index)),
                        Polyline(points.drop(-1-index), parameters.drop(-1-index).map { it - t }))
            else -> {
                val p = evaluate(t)
                Tuple(Polyline(points.take(-1-index).append(p), parameters.take(-1-index).append(t)),
                        Polyline(points.drop(-1-index).prepend(p), parameters.drop(-1-index).prepend(t).map { it - t }))
            }
        }
    }

    companion object {

        fun <C>approximate(curve: C, eps: Double = 1.0): Polyline where C : Curve, C : Differentiable {
            val derivative = curve.derivative
            fun bisection(a: Double, b: Double): List<Double> {
                val error = (curve(a).toCrisp() - curve(b).toCrisp() - (b - a) * derivative(a)).length()
                return if (error < eps) {
                    List(a, b)
                } else {
                    val c = 0.5 * a + 0.5 * b
                    bisection(c, b).prependAll(bisection(a, c).init())
                }
            }

            val parameters = bisection(curve.domain.begin, curve.domain.end).toArray()

            return Polyline(parameters.map(curve))
        }
    }
}

data class PolylineJson(val points: kotlin.Array<PointJson>) {

    companion object {

        fun toJson(polyline: Polyline): String = prettyGson.toJson(PolylineJson(polyline.points
                .map { PointJson(it.x, it.y, it.z, it.r) }
                .toJavaArray(PointJson::class.java)))

        fun fromJson(json: String): Option<Polyline> {
            return try {
                val (ps) = prettyGson.fromJson<PolylineJson>(json)
                Option(Polyline(ps.map { Point.xyzr(it.x, it.y, it.z, it.r) }))
            } catch(e: Exception) {
                None()
            }
        }
    }
}