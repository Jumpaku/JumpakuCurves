package org.jumpaku.core.fitting

import io.vavr.API
import io.vavr.collection.Array
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.Interval


fun chordalParametrize(points: Array<Point>, range: Interval = Interval.ZERO_ONE): Array<ParamPoint> {
    if(points.isEmpty){
        return API.Array()
    }
    val ds = points.tailOption()
            .map { it.zipWith(points, { a, b -> a.toCrisp().dist(b.toCrisp()) })
                    .foldLeft(API.Array(0.0), { acc, d -> acc.append(d + acc.last())})
            } .getOrElse(API.Array(0.0))

    return ds.zipWith(points, { dist, p -> ParamPoint(p, dist/ds.last()) })
                .map { it.copy(param = range.begin.divide(it.param, range.end)) }
}


fun uniformParametrize(points: Array<Point>, range: Interval = Interval.ZERO_ONE): Array<ParamPoint> {
    if(points.isEmpty){
        return API.Array()
    }
    if(points.isSingleValued){
        return points.map { ParamPoint(it, range.begin) }
    }
    return points.zipWith(range.sample(points.size()), { point, param -> ParamPoint(point, param) })
}
