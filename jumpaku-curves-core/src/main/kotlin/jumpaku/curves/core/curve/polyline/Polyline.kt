package jumpaku.curves.core.curve.polyline

import jumpaku.curves.core.curve.*
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Transform


class Polyline(paramPoints: List<ParamPoint>) : Curve {

    val paramPoints: List<ParamPoint> = paramPoints.sortedBy { it.param }

    private val parameters: List<Double> = paramPoints.map(ParamPoint::param)

    val points: List<Point> = paramPoints.map(ParamPoint::point)

    override val domain: Interval = Interval(parameters.first(), parameters.last())

    override fun toString(): String = "Polyline(paramPoints=$paramPoints)"


    override fun invoke(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        val i = parameters.binarySearch(t)
        return if (i >= 0) points[i] else evaluateInSpan(t, -2 - i)
    }

    override fun invoke(sampler: Sampler): List<Point> {
        val params = sampler.sample(domain)
        val n = params.size
        if (points.size == 1) return List(n) { points.first() }

        val evaluated = ArrayList<Point>(n)
        val ts = params.subList(1, n - 1)
        var index = 0
        ts.forEach { t ->
            while (t >= parameters[index]) ++index
            //index = parameters.indexOfFirst { } asVavr ().indexWhere({ t < it }, index)
            evaluated += evaluateInSpan(t, index - 1)
        }

        return listOf(points.first()) + evaluated + listOf(points.last())
    }

    private fun evaluateInSpan(t: Double, index: Int): Point =
        points[index].lerp((t - parameters[index]) / (parameters[index + 1] - parameters[index]), points[index + 1])

    fun transform(a: Transform): Polyline = Polyline(paramPoints.map { it.copy(point = a(it.point)) })

    override fun toCrisp(): Polyline = Polyline(paramPoints.map { it.copy(point = it.point.toCrisp()) })

    fun reverse(): Polyline =
        Polyline(points.reversed().zip(parameters.map { domain.end + domain.begin - it }.reversed(), ::ParamPoint))

    override fun restrict(interval: Interval): Polyline = restrict(interval.begin, interval.end)

    override fun restrict(begin: Double, end: Double): Polyline = subdivide(begin).second.subdivide(end).first

    fun subdivide(t: Double): Pair<Polyline, Polyline> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val index = parameters.binarySearch(t)//asVavr().search(t)
        return when {
            index >= 0 -> Pair(Polyline(paramPoints.take(index + 1)), Polyline(paramPoints.drop(index)))
            else -> {
                val p = invoke(t)
                Pair(
                    Polyline(paramPoints.take(-1 - index) + listOf(ParamPoint(p, t))),
                    Polyline((listOf(ParamPoint(p, t)) + paramPoints.drop(-1 - index)))
                )
            }
        }
    }

    companion object {

        fun byIndices(vararg points: Point): Polyline = byIndices(points.asList())

        fun byIndices(points: List<Point>): Polyline =
            Polyline(points.mapIndexed { i, p -> ParamPoint(p, i.toDouble()) })

        fun byArcLength(points: List<Point>): Polyline {
            val arcLength = points.zipWithNext { a, b -> a.dist(b) }.sum()
            val paramPoints = chordalParametrize(points.toList())
                .tryMap { transformParams(it, range = Interval(0.0, arcLength)) }
                .tryRecover { points.map { ParamPoint(it, 0.0) } }
            return Polyline(paramPoints.orThrow())
        }

        fun byArcLength(vararg points: Point): Polyline = byArcLength(points.asList())

    }
}

