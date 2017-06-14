package org.jumpaku.core.fitting

import io.vavr.API
import io.vavr.collection.Array
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


fun chordalParametrize(points: Array<Point>): Array<TimeSeriesPoint> {
    val ds = points.zip(points.tail())
            .map { (a, b) -> a.toCrisp().dist(b.toCrisp()) }
            .foldLeft(API.Array(0.0), { acc, d -> acc.append(d + acc.last())})

    return ds.zipWith(points, { dist, p -> TimeSeriesPoint(p, dist/ds.last())})
}