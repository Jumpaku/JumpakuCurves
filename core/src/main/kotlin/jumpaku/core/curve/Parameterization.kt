package jumpaku.core.curve

import jumpaku.core.geom.Point
import jumpaku.core.geom.divide
import jumpaku.core.util.*


fun transformParams(paramPoints: List<ParamPoint>, range: Interval): Option<List<ParamPoint>> {
    if (paramPoints.size < 2) return none()

    val a0 = paramPoints.first().param
    val a1 = paramPoints.last().param
    val (b, e) = range
    return 1.0.divOption(a1 - a0).map { p -> paramPoints.map {
        it.copy(param = b.divide((it.param - a0)*p, e))
    } }
}

fun uniformParametrize(points: List<Point>): List<ParamPoint> = when {
    points.isEmpty() -> emptyList()
    points.size == 1 -> points.map { ParamPoint(it, 0.0) }
    else -> points.zip(0..(points.size - 1)) { point, param -> ParamPoint(point, param.toDouble()) }
}

fun chordalParametrize(points: List<Point>): List<ParamPoint> {
    if(points.isEmpty()) return emptyList()

    val ds = points.asVavr().tailOption().toJOpt()
            .map {
                it.zipWith(points) { a, b -> a.dist(b) }
                        .foldLeft(listOf(0.0)) { acc, d -> acc + (d + acc.last())}
            } .orDefault(listOf(0.0))

    return ds.zip(points) { arcLength, point -> ParamPoint(point, arcLength) }
}
