package jumpaku.fsc.generate

import io.vavr.collection.Stream
import org.apache.commons.math3.util.FastMath
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.fsc.generate.fit.BezierFitter
import jumpaku.core.curve.chordalParametrize
import jumpaku.core.curve.transformParams
import jumpaku.core.curve.uniformParametrize
import jumpaku.core.util.*


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

    fun prepare(crispData: List<ParamPoint>): List<ParamPoint> {
        require(crispData.size >= 2) { "data size(${crispData.size}) < 2" }

        return  crispData.sortedBy(ParamPoint::param)
                .run { fill(this, maxParamSpan) }
                .run { extendFront(this, innerSpan, outerSpan, degree) }
                .run { extendBack(this, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun fill(sortedData: List<ParamPoint>, maxParamSpan: Double): List<ParamPoint> {
            require(sortedData.size >= 2) { "sortedData size is too few" }

            val data = sortedData.asVavr()
            return data.zip(data.tail())
                    .flatMap { (a, b) ->
                        val nSamples = FastMath.ceil((b.param - a.param) / maxParamSpan).toInt() + 1
                        Stream.range(0, nSamples - 1).map { a.divide(it / (nSamples - 1.0), b) }
                    }.append(data.last())
                    .asKt()
        }

        fun extendFront(sortedData: List<ParamPoint>,
                        innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): List<ParamPoint> {
            require(sortedData.size >= 2) { "sortedData size(${sortedData.size} is too few" }
            require(innerSpan > 0.0 && outerSpan > 0.0) {
                "innerSpan($innerSpan) or outerSpan($outerSpan) are negative" }

            val end = sortedData.first().param + innerSpan
            val begin = sortedData.first().param - outerSpan
            val innerData = sortedData.filter { it.param <= end }
                    .let {
                        val range = Interval(outerSpan / (outerSpan + innerSpan), 1.0)
                        transformParams(chordalParametrize(it.map { it.point }), range)
                                .orOption { transformParams(uniformParametrize(it.map { it.point }), range) }
                    }.orThrow()
            val bezier = BezierFitter(degree).fit(innerData).subdivide(outerSpan/(outerSpan+innerSpan))._1()
            val outerData = bezier.sample(Math.ceil(innerData.size*innerSpan/outerSpan).toInt())
            return transformParams(outerData, Interval(begin, begin + outerSpan)).orThrow()
                    .asVavr().init()
                    .appendAll(sortedData).asKt()
        }

        fun extendBack(sortedData: List<ParamPoint>,
                       innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): List<ParamPoint> {
            require(sortedData.size >= 2) { "sortedData size is too few" }
            require(innerSpan > 0.0 && outerSpan > 0.0) {
                "innerSpan($innerSpan) or outerSpan($outerSpan) are negative" }

            val begin = sortedData.last().param - innerSpan
            val end = sortedData.last().param + outerSpan
            val innerData = sortedData.filter { it.param >= begin }
                    .let {
                        val range = Interval(0.0, innerSpan / (outerSpan + innerSpan))
                        transformParams(chordalParametrize(it.map { it.point }), range)
                                .orOption { transformParams(uniformParametrize(it.map { it.point }), range) }
                    }.orThrow()
            val bezier = BezierFitter(degree).fit(innerData).subdivide(innerSpan/(innerSpan+outerSpan))._2()
            val outerData = bezier.sample(Math.ceil(innerData.size/innerSpan*outerSpan).toInt())
            return transformParams(outerData, Interval(end - outerSpan, end)).orThrow()
                    .asVavr().tail()
                    .prependAll(sortedData).asKt()
        }
    }
}