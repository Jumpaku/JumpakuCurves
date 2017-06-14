package org.jumpaku.core.fsci

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.divide
import org.jumpaku.core.fitting.BezierFitting
import org.jumpaku.core.fitting.chordalParametrize
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

class DataModification(
        val maximumSpan: Double,
        val innerSpan: Double,
        val outerSpan: Double = innerSpan,
        val degree: Int = 2) {

    fun modify(crispData: Array<TimeSeriesPoint>): Array<TimeSeriesPoint> {
        return  crispData.sortBy(TimeSeriesPoint::time)
                .run { interpolate(this, maximumSpan) }
                .run { extrapolateFront(this, innerSpan, outerSpan, degree) }
                .run { extrapolateBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun interpolate(sortedData: Array<TimeSeriesPoint>, maximumSpan: Double): Array<TimeSeriesPoint> {
            return sortedData.zip(sortedData.tail())
                    .flatMap { (a, b) ->
                        val nSamples = FastMath.ceil((b.time - a.time) / maximumSpan).toInt() + 1
                        Stream.range(0, nSamples - 1).map { a.divide(it / (nSamples - 1.0), b) }
                    }.append(sortedData.last())
        }

        fun extrapolateFront(sortedData: Array<TimeSeriesPoint>,
                             innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val end = sortedData.head().time + innerSpan
            val begin = sortedData.head().time - outerSpan
            val innerData  = sortedData.filter { it.time <= end } .map { (p, _) -> p }
            val bezier = BezierFitting(degree).fit(chordalParametrize(innerData)
                    .map { it.copy(time = (it.time*innerSpan+outerSpan)/(outerSpan+innerSpan)) })
            val extrapolated = bezier
                    .subdivide(outerSpan/(outerSpan+innerSpan))._1()
                    .evaluateAll(Math.ceil(innerData.size()*innerSpan/outerSpan).toInt())
            return chordalParametrize(extrapolated)
                    .init()
                    .map { it.copy(time = (it.time*outerSpan)/(outerSpan+innerSpan)) }
                    .map { it.copy(time = begin.divide(it.time, end)) }
                    .filter { it.time.isFinite() }
                    .appendAll(sortedData)
        }

        fun extrapolateBack(sortedData: Array<TimeSeriesPoint>,
                            innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<TimeSeriesPoint> {
            val begin = sortedData.last().time - innerSpan
            val end = sortedData.last().time + outerSpan
            val innerData = sortedData.filter { it.time >= begin } .map { (p, _) -> p }
            val bezier = BezierFitting(degree).fit(chordalParametrize(innerData)
                    .map { it.copy(time = it.time*innerSpan/(outerSpan+innerSpan)) })
            val extrapolated = bezier
                    .subdivide(innerSpan/(innerSpan+outerSpan))._2()
                    .evaluateAll(Math.ceil(innerData.size()/innerSpan*outerSpan).toInt())
            return chordalParametrize(extrapolated)
                    .tail()
                    .map { it.copy(time = (innerSpan + it.time*outerSpan)/(outerSpan+innerSpan)) }
                    .map { it.copy(time = begin.divide(it.time, end)) }
                    .filter { it.time.isFinite() }
                    .prependAll(sortedData)
        }
    }
}