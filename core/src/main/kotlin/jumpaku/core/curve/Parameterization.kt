package jumpaku.core.curve

import jumpaku.core.geom.Point
import jumpaku.core.geom.divide
import jumpaku.core.util.Result
import jumpaku.core.util.result
import java.util.*


fun transformParams(paramPoints: List<ParamPoint>, range: Interval): Result<List<ParamPoint>> = result {
    require(paramPoints.size >= 2) { "must be paramPoints.size(${paramPoints.size}) >= 2" }

    val a0 = paramPoints.first().param
    val a1 = paramPoints.last().param
    require((a1 - a0).let { range.span.div(it) }.isFinite()) {
        "paramPoints.first().param($a0) is close to paramPoints.last().param($a1)"
    }

    val (b, e) = range
    paramPoints.map { it.copy(param = b.divide((it.param - a0)/(a1 - a0), e).coerceIn(range)) }
}

/**
 * Parametrizes points with parameters in [0, 1] uniformly.
 */
fun uniformParametrize(points: List<Point>): Result<List<ParamPoint>> = result {
    val n = points.size
    require(n >= 2) { "must be points.size($n) >= 2" }
    points.withIndex().map { (i, p) -> ParamPoint(p, i / (n - 1.0)) }
}

/**
 * Parametrizes points with parameters of arc-length ratio in [0, 1].
 */
fun chordalParametrize(points: List<Point>): Result<List<ParamPoint>> = result {
    val n = points.distinctBy { Objects.hash(it.x, it.y, it.z) }.size
    require(n >= 2) { "must be points.size($n) >= 2" }
    points.zipWithNext()
            .fold(listOf(ParamPoint(points.first(), 0.0))) { acc, (a, b) ->
                acc + ParamPoint(b, a.dist(b) + acc.last().param)
            }
}.tryFlatMap { transformParams(it, Interval.ZERO_ONE) }
