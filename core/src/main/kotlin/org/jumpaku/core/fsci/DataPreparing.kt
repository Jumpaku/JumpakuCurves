package org.jumpaku.core.fsci

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.fitting.ParamPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.fitting.BezierFitting
import org.jumpaku.core.fitting.chordalParametrize
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


/**
 * Prepares data before FSC generation.
 * Given data is time series point sequence.
 * The data may have lacks of points, which causes singular matrix.
 * At beginning point and end point, fuzziness of generated FSC may become too large.
 * This is because velocity or acceleration of fitted curve may change suddenly, at these points.
 *
 * To avoid these problems, before FSC generation, lacks of data should be filled by linear interpolation.
 * And data points around beginning point and end point should be extended by quadratic bezier fitting.
 */
class DataPreparing(
        val maximumSpan: Double,
        val innerSpan: Double,
        val outerSpan: Double = innerSpan,
        val degree: Int = 2) {

    fun prepare(crispData: Array<ParamPoint>): Array<ParamPoint> {
        return  crispData.sortBy(ParamPoint::param)
                .run { fill(this, maximumSpan) }
                .run { extendFront(this, innerSpan, outerSpan, degree) }
                .run { extendBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun fill(sortedData: Array<ParamPoint>, maximumSpan: Double): Array<ParamPoint> {
            return sortedData.zip(sortedData.tail())
                    .flatMap { (a, b) ->
                        val nSamples = FastMath.ceil((b.param - a.param) / maximumSpan).toInt() + 1
                        Stream.range(0, nSamples - 1).map { a.divide(it / (nSamples - 1.0), b) }
                    }.append(sortedData.last())
        }

        fun extendFront(sortedData: Array<ParamPoint>,
                        innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<ParamPoint> {
            val end = sortedData.head().param + innerSpan
            val begin = sortedData.head().param - outerSpan
            val innerPoints  = sortedData.filter { it.param <= end } .map { (p, _) -> p }
            val bezierSubDomain = Interval(outerSpan/(outerSpan+innerSpan), 1.0)
            val chordalData = chordalParametrize(innerPoints, bezierSubDomain)
                    .let { fill(it, bezierSubDomain.span/degree) }
            val bezier = BezierFitting(degree).fit(chordalData)
            val extrapolated = bezier
                    .subdivide(outerSpan/(outerSpan+innerSpan)).head()
                    .evaluateAll(Math.ceil(chordalData.size()*innerSpan/outerSpan).toInt())
            return chordalParametrize(extrapolated, Interval(begin, begin + outerSpan))
                    .let { fill(it, bezierSubDomain.span/degree) }
                    .init()
                    .filter { it.param.isFinite() }
                    .appendAll(sortedData)
        }

        fun extendBack(sortedData: Array<ParamPoint>,
                       innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<ParamPoint> {
            val begin = sortedData.last().param - innerSpan
            val end = sortedData.last().param + outerSpan
            val innerPoints = sortedData.filter { it.param >= begin } .map { (p, _) -> p }
            val bezierSubDomain = Interval(0.0, innerSpan/(outerSpan+innerSpan))
            val chordalData = chordalParametrize(innerPoints, bezierSubDomain)
                    .let { fill(it, bezierSubDomain.span/degree) }
            val bezier = BezierFitting(degree).fit(chordalData)
            val extrapolated = bezier
                    .subdivide(innerSpan/(innerSpan+outerSpan)).last()
                    .evaluateAll(Math.ceil(innerPoints.size()/innerSpan*outerSpan).toInt())
            return chordalParametrize(extrapolated, Interval(end - outerSpan, end))
                    .let { fill(it, bezierSubDomain.span/degree) }
                    .tail()
                    .filter { it.param.isFinite() }
                    .prependAll(sortedData)
        }
    }
}