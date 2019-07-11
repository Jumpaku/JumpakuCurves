package jumpaku.curves.core.curve.polyline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.util.asVavr


class Polyline(paramPoints: Iterable<ParamPoint>) : Curve, ToJson {

    val paramPoints: List<ParamPoint> = paramPoints.sortedBy { it.param }

    private val parameters: List<Double> = paramPoints.map(ParamPoint::param)

    val points: List<Point> = paramPoints.map(ParamPoint::point)

    init {
        require(points.size == parameters.size) { "points.size() != parameters.size()" }
    }

    override val domain: Interval = Interval(parameters.first(), parameters.last())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("paramPoints" to jsonArray(paramPoints.map { it.toJson() }))

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        val i = parameters.asVavr().search(t)
        return if (i >= 0) points[i] else evaluateInSpan(t, -2 - i)
    }

    override fun evaluateAll(n: Int): List<Point> {
        if (points.size == 1) return List(n) { points.first() }

        val evaluated = ArrayList<Point>(n)
        val ts = domain.sample(n).asVavr().subSequence(1, n - 1)
        var index = 0
        ts.forEach { t ->
            index = parameters.asVavr().indexWhere({ t < it }, index)
            evaluated += evaluateInSpan(t, index - 1)
        }

        return listOf(points.first()) + evaluated + listOf(points.last())
    }

    private fun evaluateInSpan(t: Double, index: Int): Point =
            points[index].lerp((t - parameters[index]) / (parameters[index + 1] - parameters[index]), points[index + 1])

    fun transform(a: Transform): Polyline = byArcLength(points.map(a::invoke))

    override fun toCrisp(): Polyline = Polyline(paramPoints.map { it.copy(point = it.point.toCrisp()) })

    fun reverse(): Polyline =
            Polyline(points.reversed().zip(parameters.map { domain.end + domain.begin - it }.reversed(), ::ParamPoint))

    fun restrict(i: Interval): Polyline = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Polyline = subdivide(begin).second.subdivide(end).first

    fun subdivide(t: Double): Pair<Polyline, Polyline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val index = parameters.asVavr().search(t)
        return when {
            index >= 0 -> Pair(Polyline(paramPoints.take(index + 1)), Polyline(paramPoints.drop(index)))
            else -> {
                val p = evaluate(t)
                Pair(Polyline(paramPoints.take(-1 - index) + listOf(ParamPoint(p, t))),
                        Polyline((listOf(ParamPoint(p, t)) + paramPoints.drop(-1 - index))))
            }
        }
    }

    companion object {

        fun of(points: Iterable<Point>): Polyline =
                Polyline(points.withIndex().map { (i, p) -> ParamPoint(p, i.toDouble()) })

        fun byArcLength(points: Iterable<Point>): Polyline {
            val arcLength = points.zipWithNext { a, b -> a.dist(b) }.sum()
            val paramPoints = chordalParametrize(points.toList())
                    .tryMap { transformParams(it, range = Interval(0.0, arcLength)) }
                    .tryRecover { points.map { ParamPoint(it, 0.0) } }
            return Polyline(paramPoints.orThrow())
        }

        fun byArcLength(vararg points: Point): Polyline = byArcLength(points.asIterable())

        fun fromJson(json: JsonElement): Polyline = Polyline(json["paramPoints"].array.map { ParamPoint.fromJson(it) })
    }
}
