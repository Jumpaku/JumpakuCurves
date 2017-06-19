package org.jumpaku.core.curve.polyline

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.List
import io.vavr.collection.Stream
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.fitting.ParamPoint
import org.jumpaku.core.fitting.chordalParametrize
import org.jumpaku.core.json.prettyGson


class Polyline (val points: Array<Point>, private val parameters: Array<Double>) : FuzzyCurve, CrispTransformable {

    init {
        if(points.size() != parameters.size()){
            throw IllegalArgumentException("points.size() != parameters.size()")
        }
    }

    override val domain: Interval = Interval(parameters.head(), parameters.last())

    constructor(points: Array<Point>) :this(points,
            chordalParametrize(points).map(ParamPoint::param).run {
                points.tailOption()
                        .map { it.zipWith(points, { a, b -> a.toCrisp().dist(b.toCrisp()) }).sum().toDouble() }
                        .map { this@run.map(it::times) }
                        .getOrElse(Array(0.0))
            })

    constructor(points: Iterable<Point>) : this(Array.ofAll(points))

    constructor(vararg points: Point) : this(Array(*points))

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): PolylineJson = PolylineJson(this)

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        val i = parameters.search(t)
        return if(i >= 0) {
            points[i]
        }
        else {
            evaluateInSpan(t, -2 - i)
        }
    }

    override fun evaluateAll(n: Int): Array<Point> = sampleArcLength(n)

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

    override fun crispTransform(a: Transform): Polyline = Polyline(points.map { a(it.toCrisp()) })

    fun reverse(): Polyline = Polyline(points.reverse(), parameters.map { domain.end + domain.begin - it }.reverse())

    fun restrict(i: Interval): Polyline = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Polyline = subdivide(begin).last().subdivide(end-begin).head()

    fun subdivide(t: Double): Array<Polyline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val index = parameters.search(t)
        return when{
            index >= 0 ->
                Array(Polyline(points.take(index+1), parameters.take(index+1)),
                        Polyline(points.drop(index), parameters.drop(index).map { it - t }))
            Precision.equals(parameters[-2-index], t, 1.0e-10) ->
                Array(Polyline(points.take(-1-index), parameters.take(-1-index)),
                        Polyline(points.drop(-2-index), parameters.drop(-2-index).map { it - t }))
            Precision.equals(parameters[-1-index], t, 1.0e-10) ->
                Array(Polyline(points.take(-index), parameters.take(-index)),
                        Polyline(points.drop(-1-index), parameters.drop(-1-index).map { it - t }))
            else -> {
                val p = evaluate(t)
                Array(Polyline(points.take(-1-index).append(p), parameters.take(-1-index).append(t)),
                        Polyline(points.drop(-1-index).prepend(p), parameters.drop(-1-index).prepend(t).map { it - t }))
            }
        }
    }

    companion object {

        fun <C> approximate(curve: C, eps: Double = 1.0): Polyline where C : Curve, C : Differentiable {
            val derivative = curve.derivative
            fun bisection(a: Double, b: Double): List<Double> {
                val error = (curve(a).toCrisp() - curve(b).toCrisp() - (b - a) * derivative(a)).length()
                return if (error < eps) {
                    List(a, b)
                } else {
                    val c = a.divide(0.5, b)
                    bisection(c, b).prependAll(bisection(a, c).init())
                }
            }

            val parameters = bisection(curve.domain.begin, curve.domain.end).toArray()

            return Polyline(parameters.map(curve))
        }
    }
}

data class PolylineJson(private val points: kotlin.collections.List<PointJson>) {

    constructor(polyline: Polyline) : this(polyline.points.map(Point::json).toJavaList())

    fun polyline(): Polyline = Polyline(points.map(PointJson::point))
}