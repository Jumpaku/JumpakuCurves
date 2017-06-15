package org.jumpaku.core.fitting

import io.vavr.API
import io.vavr.collection.Array
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2



fun chordalParametrize(points: Array<Point>, range: Interval = Interval.ZERO_ONE): Array<TimeSeriesPoint> {
    val ds = points.zip(points.tail())
            .map { (a, b) -> a.toCrisp().dist(b.toCrisp()) }
            .foldLeft(API.Array(0.0), { acc, d -> acc.append(d + acc.last())})

    return ds.zipWith(points, { dist, p -> TimeSeriesPoint(p, dist/ds.last())})
            .map { it.copy(time = range.begin.divide(it.time, range.end)) }
}


fun uniformParametrize(points: Array<Point>, range: Interval = Interval.ZERO_ONE): Array<TimeSeriesPoint> {
    return points.zipWith(range.sample(points.size()), ::TimeSeriesPoint)
}
