package org.jumpaku.core.fsci

import io.vavr.collection.Array
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.fitting.BezierFitting
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

class DataModification(
        val maximumSpan: Double,
        val innerSpan: Double,
        val outerSpan: Double = innerSpan,
        val degree: Int = 2) {

    fun modify(crispData: Array<TimeSeriesPoint>): Array<TimeSeriesPoint> {
        val sortedData = crispData.sortBy(TimeSeriesPoint::time)
        return interpolate(sortedData, maximumSpan)
                .run { extrapolateFront(this, innerSpan, outerSpan, degree) }
                .run { extrapolateBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun interpolate(sortedData: Array<TimeSeriesPoint>, maximumSpan: Double): Array<TimeSeriesPoint> {
            return sortedData.zip(sortedData.tail())
                    .flatMap { (a, b) ->
                        val n = FastMath.ceil((b.time - a.time) / maximumSpan).toInt()
                        (1..n).map { a.divide(it.toDouble() / n, b) }
                    }.prepend(sortedData.head())
        }

        fun extrapolateFront(sortedData: Array<TimeSeriesPoint>,
                             innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val begin = sortedData.head().time
            val innerData = sortedData
                    .takeWhile { it.time < begin + outerSpan }
                    .map { it.copy(time = (it.time - begin + outerSpan)/(outerSpan + innerSpan)) }
            val bezier = BezierFitting(degree).fit(innerData)
            val prepend = bezier.domain
                    .sample(FastMath.ceil(innerSpan / (outerSpan + innerSpan)*innerData.size()).toInt())
                    .takeWhile { (outerSpan + innerSpan)*it < outerSpan }
                    .map { t -> TimeSeriesPoint(bezier(t), (1 - t) * (begin - innerSpan) + t * (begin + innerSpan)) }
            return sortedData.prependAll(prepend)
        }

        fun extrapolateBack(sortedData: Array<TimeSeriesPoint>,
                            innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val end = sortedData.last().time
            val innerData = sortedData
                    .dropWhile { it.time < end - innerSpan }
                    .map { it.copy(time = (it.time - end + innerSpan)/(outerSpan + innerSpan)) }
            val bezier = BezierFitting(degree).fit(innerData)
            val append = bezier.domain
                    .sample(FastMath.ceil((outerSpan) / (innerSpan + outerSpan)*innerData.size()).toInt())
                    .dropWhile { (outerSpan + innerSpan)*it <= innerSpan }
                    .map { t -> TimeSeriesPoint(bezier(t), (1 - t) * (end - innerSpan) + t * (end + outerSpan)) }
            return sortedData.appendAll(append)
        }
    }
}