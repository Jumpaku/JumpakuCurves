package jumpaku.core.curve.polyline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.curve.*
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.util.Result
import jumpaku.core.util.asKt
import jumpaku.core.util.asVavr
import jumpaku.core.util.result
import org.apache.commons.math3.util.Precision


/**
 * Polyline parametrized by arc-length.
 */
class Polyline(paramPoints: Iterable<ParamPoint>) : Curve, ToJson {

    private val paramPoints: List<ParamPoint> = paramPoints.toList()

    private val parameters: List<Double> = paramPoints.map(ParamPoint::param)

    val points: List<Point> = paramPoints.map(ParamPoint::point)

    init {
        require(points.size == parameters.size){ "points.size() != parameters.size()" }
    }

    override val domain: Interval = Interval(parameters.first(), parameters.last())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("points" to jsonArray(points.map { it.toJson() }))

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        val i = parameters.asVavr().search(t)
        return if(i >= 0) points[i] else evaluateInSpan(t, -2 - i)
    }

    override fun evaluateAll(n: Int): List<Point> {
        if (points.size == 1) return List(n) { points.first() }

        val evaluated = ArrayList<Point>(n)
        val ts = domain.sample(n).asVavr().subSequence(1, n-1)
        var index = 0
        ts.forEach { t ->
            index = parameters.asVavr().indexWhere({ t < it }, index)
            evaluated += evaluateInSpan(t, index-1)
        }

        return listOf(points.first()) + evaluated + listOf(points.last())
    }

    private fun evaluateInSpan(t: Double, index: Int): Point = points[index].divide(
                (t - parameters[index]) / (parameters[index+1] - parameters[index]), points[index+1])

    fun transform(a: Transform): Polyline = of(points.map(a::invoke))

    override fun toCrisp(): Polyline = Polyline(paramPoints.map { it.copy(point = it.point.toCrisp()) })

    fun reverse(): Polyline = Polyline(points.reversed().zip(parameters.map { domain.end + domain.begin - it }.reversed(), ::ParamPoint))

    fun restrict(i: Interval): Polyline = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Polyline = subdivide(begin)._2().subdivide(end-begin)._1()

    fun subdivide(t: Double): Tuple2<Polyline, Polyline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val index = parameters.asVavr().search(t)
        return when{
            index >= 0 ->
                Tuple2(Polyline(paramPoints.take(index + 1)),
                        Polyline(paramPoints.drop(index).map { it.copy(param = it.param - t) }))
            Precision.equals(parameters[-2-index], t, 1.0e-10) ->
                Tuple2(Polyline(paramPoints.take(-1 - index)),
                        Polyline(paramPoints.drop(-2 - index).map { it.copy(param = it.param - t) }))
            Precision.equals(parameters[-1-index], t, 1.0e-10) ->
                Tuple2(Polyline(paramPoints.take(-index)),
                        Polyline(paramPoints.drop(-1 - index).map { it.copy(param = it.param - t) }))
            else -> {
                val p = evaluate(t)
                Tuple2(
                        paramPoints.take(-1 - index) + listOf(ParamPoint(p, t)),
                        (listOf(ParamPoint(p, t)) + paramPoints.drop(-1 - index))
                ).map(::Polyline, { Polyline(it.map { it.copy(param = it.param - t) }) })
            }
        }
    }

    companion object {

        fun of(points: Iterable<Point>): Polyline {
            val arcLength = points.zipWithNext { a, b -> a.dist(b) }.sum()
            val paramPoints = chordalParametrize(points.toList())
                    .tryFlatMap { transformParams(it, Interval(0.0, arcLength)) }
            return Polyline(paramPoints.orThrow())
        }

        fun of(vararg points: Point): Polyline = of(points.asIterable())

        fun fromJson(json: JsonElement): Result<Polyline> = result {
            of(json["points"].array.flatMap { Point.fromJson(it).value() })
        }
    }
}
