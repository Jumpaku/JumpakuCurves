package org.jumpaku.core.fsci

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.fitting.BezierFitting
import org.jumpaku.core.fitting.chordalParametrize
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

class DataPreparing(
        val maximumSpan: Double,
        val innerSpan: Double,
        val outerSpan: Double = innerSpan,
        val degree: Int = 2) {

    fun prepare(crispData: Array<TimeSeriesPoint>): Array<TimeSeriesPoint> {
        return  crispData.sortBy(TimeSeriesPoint::time)
                .run { fill(this, maximumSpan) }
                .run { extendFront(this, innerSpan, outerSpan, degree) }
                .run { extendBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun fill(sortedData: Array<TimeSeriesPoint>, maximumSpan: Double): Array<TimeSeriesPoint> {
            return sortedData.zip(sortedData.tail())
                    .flatMap { (a, b) ->
                        val nSamples = FastMath.ceil((b.time - a.time) / maximumSpan).toInt() + 1
                        Stream.range(0, nSamples - 1).map { a.divide(it / (nSamples - 1.0), b) }
                    }.append(sortedData.last())
        }

        fun extendFront(sortedData: Array<TimeSeriesPoint>,
                        innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val end = sortedData.head().time + innerSpan
            val begin = sortedData.head().time - outerSpan
            val innerPoints  = sortedData.filter { it.time <= end } .map { (p, _) -> p }
            val bezierSubDomain = Interval(outerSpan/(outerSpan+innerSpan), 1.0)
            val chordalData = chordalParametrize(innerPoints, bezierSubDomain)
                    .let { fill(it, bezierSubDomain.span/degree) }
            val bezier = BezierFitting(degree).fit(chordalData)
            val extrapolated = bezier
                    .subdivide(outerSpan/(outerSpan+innerSpan))._1()
                    .evaluateAll(Math.ceil(chordalData.size()*innerSpan/outerSpan).toInt())
            return chordalParametrize(extrapolated, Interval(begin, begin + outerSpan))
                    .let { fill(it, bezierSubDomain.span/degree) }
                    .init()
                    .filter { it.time.isFinite() }
                    .appendAll(sortedData)
        }

        fun extendBack(sortedData: Array<TimeSeriesPoint>,
                       innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val begin = sortedData.last().time - innerSpan
            val end = sortedData.last().time + outerSpan
            val innerPoints = sortedData.filter { it.time >= begin } .map { (p, _) -> p }
            val bezierSubDomain = Interval(0.0, innerSpan/(outerSpan+innerSpan))
            val chordalData = chordalParametrize(innerPoints, bezierSubDomain)
                    .let { fill(it, bezierSubDomain.span/degree) }
            val bezier = BezierFitting(degree).fit(chordalData)
            val extrapolated = bezier
                    .subdivide(innerSpan/(innerSpan+outerSpan))._2()
                    .evaluateAll(Math.ceil(innerPoints.size()/innerSpan*outerSpan).toInt())
            return chordalParametrize(extrapolated, Interval(end - outerSpan, end))
                    .let { fill(it, bezierSubDomain.span/degree) }
                    .tail()
                    .filter { it.time.isFinite() }
                    .prependAll(sortedData)
        }
    }
}