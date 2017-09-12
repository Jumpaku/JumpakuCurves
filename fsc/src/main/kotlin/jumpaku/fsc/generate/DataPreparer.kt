package jumpaku.fsc.generate

import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.fit.BezierFitter
import jumpaku.core.fit.chordalParametrize
import jumpaku.core.fit.transformParams
import jumpaku.core.util.component1
import jumpaku.core.util.component2


/**
 * Prepares data before FSC generation.
 * Given data is param series point sequence.
 * The data may have lacks of points, which causes singular matrix.
 * At beginning point and end point, fuzziness of generated FSC may become too large.
 * This is because velocity or acceleration of fitted curve may change suddenly, at these points.
 *
 * To avoid these problems, before FSC generation, lacks of data should be filled by linear interpolation.
 * And data points around beginning point and end point should be extended by quadratic bezier fitting.
 */
class DataPreparer(
        val maxParamSpan: Double,
        val innerSpan: Double,
        val outerSpan: Double = innerSpan,
        val degree: Int = 2) {

    fun prepare(crispData: Array<ParamPoint>): Array<ParamPoint> {
        require(crispData.size() >= 2) { "data size(${crispData.size()}) < 2" }

        return  crispData.sortBy(ParamPoint::param)
                .run { fill(this, maxParamSpan) }
                .run { extendFront(this, innerSpan, outerSpan, degree) }
                .run { extendBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun fill(sortedData: Array<ParamPoint>, maxParamSpan: Double): Array<ParamPoint> {
            require(sortedData.size() >= 2) { "sortedData size is too few" }

            return sortedData.zip(sortedData.tail())
                    .flatMap { (a, b) ->
                        val nSamples = FastMath.ceil((b.param - a.param) / maxParamSpan).toInt() + 1
                        Stream.range(0, nSamples - 1).map { a.divide(it / (nSamples - 1.0), b) }
                    }.append(sortedData.last())
        }

        fun extendFront(sortedData: Array<ParamPoint>,
                        innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<ParamPoint> {
            require(sortedData.size() >= 2) { "sortedData size(${sortedData.size()} is too few" }

            val end = sortedData.head().param + innerSpan
            val begin = sortedData.head().param - outerSpan
            val innerData = sortedData.filter { it.param <= end }
                    .let { chordalParametrize(it.map { it.point }) }
                    .let { transformParams(it, Interval(outerSpan / (outerSpan + innerSpan), 1.0)) }
            val bezier = BezierFitter(degree).fit(innerData).subdivide(outerSpan/(outerSpan+innerSpan))._1()
            val outerData = bezier.sample(Math.ceil(innerData.size()*innerSpan/outerSpan).toInt())
            return transformParams(outerData, Interval(begin, begin + outerSpan))
                    .init()
                    .appendAll(sortedData)
        }

        fun extendBack(sortedData: Array<ParamPoint>,
                       innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): Array<ParamPoint> {
            require(sortedData.size() >= 2) { "sortedData size is too few" }

            val begin = sortedData.last().param - innerSpan
            val end = sortedData.last().param + outerSpan
            val innerData = sortedData.filter { it.param >= begin }
                    .let { chordalParametrize(it.map { it.point }) }
                    .let { transformParams(it, Interval(0.0, innerSpan / (outerSpan + innerSpan))) }
            val bezier = BezierFitter(degree).fit(innerData).subdivide(innerSpan/(innerSpan+outerSpan))._2()
            val outerData = bezier.sample(Math.ceil(innerData.size()/innerSpan*outerSpan).toInt())
            return transformParams(outerData, Interval(end - outerSpan, end))
                    .tail()
                    .prependAll(sortedData)
        }
    }
}