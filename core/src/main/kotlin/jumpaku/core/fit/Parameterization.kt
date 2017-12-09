package jumpaku.core.fit

import io.vavr.API
import io.vavr.collection.Array
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.affine.divide
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.util.divOption
import jumpaku.core.util.divOrElse


fun transformParams(paramPoints: Array<ParamPoint>, range: Interval): Option<Array<ParamPoint>> {
    if (paramPoints.size() < 2){
        return Option.none()
    }
    val a0 = paramPoints.head().param
    val a1 = paramPoints.last().param
    val (b, e) = range
    return 1.0.divOption(a1 - a0).map { p -> paramPoints.map {
        it.copy(param = b.divide((it.param - a0)*p, e))
    } }
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
