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
        val spanShouldBeFilled: Double,
        val extendInnerSpan: Double,
        val extendOuterSpan: Double,
        val extendDegree: Int) {

    init {
        require(extendInnerSpan > 0.0) { "must be extendInnerSpan($extendInnerSpan) > 0" }
        require(extendOuterSpan > 0.0) { "must be extendOuterSpan($extendOuterSpan) > 0" }
    }

    fun prepare(crispData: List<ParamPoint>): List<ParamPoint> {
        require(crispData.size >= 2) { "data.size == ${crispData.size}, too few data" }

        return crispData.sortedBy(ParamPoint::param)
                .let { fill(it) }
                .let { extendFront(it) }
                .let { extendBack(it) }
    }

    fun fill(sortedData: List<ParamPoint>): List<ParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        return sortedData.drop(1).fold(mutableListOf(sortedData.first())) { filled, next ->
            val prev = filled.last()
            val n = FastMath.ceil((next.param - prev.param) / spanShouldBeFilled)
            filled += (1..n.toInt()).map { prev.lerp(it/n, next) }
            filled
        }
    }

    fun extendFront(sortedData: List<ParamPoint>): List<ParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        val first = sortedData.first().param
        val innerOuterBSpline = first.let { Interval(it - extendOuterSpan, it + extendInnerSpan) }
        val (begin, end) = innerOuterBSpline
        val innerPoints = sortedData.filter { it.param <= end }
        val outerBezier = Interval(0.0, extendOuterSpan / (extendOuterSpan + extendInnerSpan))
        val outerBSpline = Interval(begin, first)
        val outerData = extend(innerPoints, innerOuterBSpline, outerBezier, outerBSpline)
        return outerData + sortedData
    }

    fun extendBack(sortedData: List<ParamPoint>): List<ParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        val last = sortedData.last().param
        val innerOuterBSpline = last.let { Interval(it - extendInnerSpan, it + extendOuterSpan) }
        val (begin, end) = innerOuterBSpline
        val innerPoints = sortedData.filter { it.param >= begin }
        val outerBezier = Interval(extendInnerSpan / (extendOuterSpan + extendInnerSpan), 1.0)
        val outerBSpline = Interval(last, end)
        val outerData = extend(innerPoints, innerOuterBSpline, outerBezier, outerBSpline)
        return sortedData + outerData
    }

    private fun extend(
            innerPoints: List<ParamPoint>,
            innerOuterBSpline: Interval,
            outerBezier: Interval,
            outerBSpline: Interval
    ): List<ParamPoint> {
        val innerData = transformParams(innerPoints, domain = innerOuterBSpline, range = Interval.ZERO_ONE)
        val bezier = BezierFitter(extendDegree).fit(innerData).restrict(outerBezier)
        val points = bezier.sample(Math.ceil(innerData.size * extendOuterSpan / extendInnerSpan).toInt())
        return transformParams(points, range = outerBSpline)
    }
}