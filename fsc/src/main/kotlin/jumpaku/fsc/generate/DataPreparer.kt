package jumpaku.fsc.generate

import org.apache.commons.math3.util.FastMath
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.fsc.generate.fit.BezierFitter
import jumpaku.core.curve.transformParams


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
        require(crispData.size >= 2) { "data.size == ${crispData.size}, too few data" }

        return  crispData.sortedBy(ParamPoint::param)
                .let { fill(it, maxParamSpan) }
                .let { extendFront(it, innerSpan, outerSpan, degree) }
                .let { extendBack(it, innerSpan, outerSpan, degree) }
    }

    companion object {

        fun fill(sortedData: List<ParamPoint>, maxParamSpan: Double): List<ParamPoint> {
            require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

            return sortedData.drop(1).fold(mutableListOf(sortedData.first())) { filled, next ->
                val prev = filled.last()
                val n = FastMath.ceil((next.param - prev.param) / maxParamSpan).toInt()
                filled += (1..n).map { prev.divide(it / n.toDouble(), next) }
                filled
            }
        }

        fun extendFront(sortedData: List<ParamPoint>, innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): List<ParamPoint> {
            require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }
            require(innerSpan * outerSpan > 0.0) { "must be innerSpan($innerSpan) > 0 && outerSpan($outerSpan) > 0" }

            val first = sortedData.first().param
            val innerOuterBSpline = first.let { Interval(it - outerSpan, it + innerSpan) }
            val (begin, end) = innerOuterBSpline
            val innerPoints = sortedData.filter { it.param <= end }
            val outerBezier = Interval(0.0, outerSpan / (outerSpan + innerSpan))
            val outerBSpline = Interval(begin, first)
            val outerData = extend(degree, innerPoints, innerSpan, outerSpan, innerOuterBSpline, outerBezier, outerBSpline)
            return outerData + sortedData
        }

        fun extendBack(sortedData: List<ParamPoint>, innerSpan: Double, outerSpan: Double = innerSpan, degree: Int = 2): List<ParamPoint> {
            require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }
            require(innerSpan * outerSpan > 0.0) { "must be innerSpan($innerSpan) > 0 && outerSpan($outerSpan) > 0" }

            val last = sortedData.last().param
            val innerOuterBSpline = last.let { Interval(it - innerSpan, it + outerSpan) }
            val (begin, end) = innerOuterBSpline
            val innerPoints = sortedData.filter { it.param >= begin }
            val outerBezier = Interval(innerSpan / (outerSpan + innerSpan), 1.0)
            val outerBSpline = Interval(last, end)
            val outerData = extend(degree, innerPoints, innerSpan, outerSpan, innerOuterBSpline, outerBezier, outerBSpline)
            return sortedData + outerData
        }

        private fun extend(degree: Int, innerPoints: List<ParamPoint>, innerSpan: Double, outerSpan: Double, innerOuterBSpline: Interval, outerBezier: Interval, outerBSpline: Interval): List<ParamPoint> {
            val innerData = transformParams(innerPoints, domain = innerOuterBSpline, range = Interval.ZERO_ONE)
            val bezier = BezierFitter(degree).fit(innerData).restrict(outerBezier)
            val points = bezier.sample(Math.ceil(innerData.size * outerSpan / innerSpan).toInt())
            return transformParams(points, range = outerBSpline)
        }
    }
}