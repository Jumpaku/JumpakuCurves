package jumpaku.core.fit

import io.vavr.API
import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.affine.divide
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.util.divOrElse


fun transformParams(paramPoints: Array<ParamPoint>, range: Interval): Array<ParamPoint> {
    val a0 = paramPoints.head().param
    val a1 = paramPoints.last().param
    return paramPoints.map { it.copy(param = range.begin.divide((it.param - a0).divOrElse (a1 - a0, it.param), range.end)) }
}

fun uniformParametrize(points: Array<Point>): Array<ParamPoint> {
    if(points.isEmpty){
        return API.Array()
    }
    if(points.isSingleValued){
        return points.map { ParamPoint(it, 0.0) }
    }
    return points.zipWith((0..(points.size() - 1)).map(Int::toDouble), { point, param -> ParamPoint(point, param) })
}

fun chordalParametrize(points: Array<Point>): Array<ParamPoint> {
    if(points.isEmpty){
        return API.Array()
    }
    val ds = points.tailOption()
            .map { it.zipWith(points, { a, b -> a.dist(b) })
                    .foldLeft(API.Array(0.0), { acc, d -> acc.append(d + acc.last())})
            } .getOrElse(API.Array(0.0))

    return ds.zipWith(points, { arcLength, point -> ParamPoint(point, arcLength) })
}
