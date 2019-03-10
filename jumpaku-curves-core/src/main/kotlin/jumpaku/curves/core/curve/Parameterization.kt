package jumpaku.curves.core.curve

import jumpaku.commons.control.Result
import jumpaku.commons.control.result
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import kotlin.math.pow

fun chordalParametrize(points: List<Point>, range: Interval = Interval.ZERO_ONE, power: Double = 1.0): Result<List<ParamPoint>> = result {
    require(points.size >= 2) { "must be points.size(${points.size}) >= 2" }
    val ds = points.zipWithNext { p0, p1 -> p1.distSquare(p0).pow(power / 2) }
    val ls = ds.fold(listOf(0.0)) { acc, d -> acc + (acc.last() + d) }
    check((ls.last() / ls.last()).isFinite()) { "" }
    val (a, b) = range
    ls.zip(points) { l, p -> ParamPoint(p, a.lerp(l / ls.last(), b).coerceIn(range)) }
}

fun centripetalParametrize(points: List<Point>, range: Interval = Interval.ZERO_ONE): Result<List<ParamPoint>> =
        chordalParametrize(points, range, 0.5)

fun uniformParametrize(points: List<Point>, range: Interval = Interval.ZERO_ONE): List<ParamPoint> =
        chordalParametrize(points, range, 0.0).orThrow()

fun transformParams(
        paramPoints: List<ParamPoint>,
        domain: Interval = Interval(paramPoints.first().param, paramPoints.last().param),
        range: Interval
): List<ParamPoint> {
    require(paramPoints.size >= 2) { "must be paramPoints.size(${paramPoints.size}) >= 2" }
    require((range.span / domain.span).isFinite()) { "must be domain.span(${domain.span}) > 0.0" }
    val (a0, a1) = domain
    val (b, e) = range
    return paramPoints.map { it.copy(param = b.lerp((it.param - a0) / (a1 - a0), e).coerceIn(range)) }
}