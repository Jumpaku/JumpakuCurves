package org.jumpaku.core.curve.polyline

import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.fitting.chordalParametrize
import org.jumpaku.core.json.prettyGson


/**
 * Polyline parametrized by arc-arcLength.
 */
class Polyline (private val paramPoints: Array<ParamPoint>) : FuzzyCurve, Transformable {

    val points: Array<Point> = paramPoints.map(ParamPoint::point)

    private val parameters: Array<Double> = paramPoints.map(ParamPoint::param)

    init {
        require(points.size() == parameters.size()){ "points.size() != parameters.size()" }
    }

    override val domain: Interval = Interval(parameters.head(), parameters.last())

    constructor(points: Iterable<Point>) : this(chordalParametrize(Array.ofAll(points)))

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

    override fun evaluateAll(n: Int): Array<Point> {
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

    private fun evaluateInSpan(t: Double, index: Int): Point = points[index].divide(
                (t - parameters[index]) / (parameters[index+1] - parameters[index]), points[index+1])

    override fun transform(a: Transform): Polyline = Polyline(points.map(a))

    fun reverse(): Polyline = Polyline(points.reverse().zipWith(parameters.map { domain.end + domain.begin - it }.reverse(), ::ParamPoint))

    fun restrict(i: Interval): Polyline = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Polyline = subdivide(begin).last().subdivide(end-begin).head()

    fun subdivide(t: Double): Array<Polyline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val index = parameters.search(t)
        return when{
            index >= 0 ->
                Array(Polyline(paramPoints.take(index+1)),
                        Polyline(paramPoints.drop(index).map { it.copy(param = it.param - t) }))
            Precision.equals(parameters[-2-index], t, 1.0e-10) ->
                Array(Polyline(paramPoints.take(-1-index)),
                        Polyline(paramPoints.drop(-2-index).map { it.copy(param = it.param - t) }))
            Precision.equals(parameters[-1-index], t, 1.0e-10) ->
                Array(Polyline(paramPoints.take(-index)),
                        Polyline(paramPoints.drop(-1-index).map { it.copy(param = it.param - t) }))
            else -> {
                val p = evaluate(t)
                Array(Polyline(paramPoints.take(-1-index).append(ParamPoint(p, t))),
                        Polyline(paramPoints.drop(-1-index).prepend(ParamPoint(p, t)).map { it.copy(param = it.param - t) }))
            }
        }
    }
}

data class PolylineJson(private val points: kotlin.collections.List<PointJson>) {

    constructor(polyline: Polyline) : this(polyline.points.map(Point::json).toJavaList())

    fun polyline(): Polyline = Polyline(points.map(PointJson::point))
}