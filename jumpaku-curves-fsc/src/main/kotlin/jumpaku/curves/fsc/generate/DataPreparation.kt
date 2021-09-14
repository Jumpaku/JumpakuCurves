package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.fsc.generate.fit.weighted
import jumpaku.curves.core.geom.middle
import jumpaku.curves.fsc.generate.fit.BezierFitter
import org.apache.commons.math3.util.FastMath
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


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
fun prepareData(
    data: List<WeightedParamPoint>,
    fillSpan: Double,
    extendInnerSpan: Double,
    extendOuterSpan: Double,
    extendDegree: Int
): List<WeightedParamPoint> {
    val filled = fill(data.sortedBy { it.param }, fillSpan)
    val front = extendFront(filled, extendInnerSpan, extendOuterSpan, extendDegree)
    val back = extendBack(filled, extendInnerSpan, extendOuterSpan, extendDegree)
    return front + filled + back
}

fun fill(sortedData: List<WeightedParamPoint>, fillSpan: Double): List<WeightedParamPoint> {
    require(fillSpan > 0.0) { "must be fillSpan($fillSpan ) > 0.0" }
    require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }
    return sortedData.drop(1).fold(mutableListOf(sortedData.first())) { filled, (nextP, nextW) ->
        val (prevP, prevW) = filled.last()
        val n = FastMath.ceil((nextP.param - prevP.param) / fillSpan)
        filled += (1..n.toInt()).map { prevP.lerp(it / n, nextP).weighted(prevW.middle(nextW)) }
        filled
    }
}

fun extendFront(
    sortedData: List<WeightedParamPoint>,
    extendInnerSpan: Double,
    extendOuterSpan: Double,
    extendDegree: Int,
    fillSpan: Double = Double.MAX_VALUE
): List<WeightedParamPoint> {
    require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }
    require(extendInnerSpan > 0.0) { "must be extendInnerSpan($extendInnerSpan) > 0" }
    require(extendOuterSpan > 0.0) { "must be extendOuterSpan($extendOuterSpan) > 0" }
    require(extendDegree >= 0) { "must be extendDegree($extendDegree ) >= 0" }
    require(fillSpan > 0.0) { "must be fillSpan($fillSpan ) > 0" }

    val first = sortedData.first()
    val innerOuterBSpline = first.param.let { Interval(it - extendOuterSpan, it + extendInnerSpan) }
    val (begin, end) = innerOuterBSpline
    val innerPoints = sortedData.filter { it.param <= end }
    val outerBezier = Interval(0.0, extendOuterSpan / (extendOuterSpan + extendInnerSpan))
    val outerBSpline = Interval(begin, first.param)
    return extend(
        innerPoints,
        innerOuterBSpline,
        outerBezier,
        outerBSpline,
        first.weight,
        extendInnerSpan,
        extendOuterSpan,
        extendDegree,
        fillSpan
    )
}

fun extendBack(
    sortedData: List<WeightedParamPoint>,
    extendInnerSpan: Double,
    extendOuterSpan: Double,
    extendDegree: Int,
    fillSpan: Double = Double.MAX_VALUE
): List<WeightedParamPoint> {
    require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }
    require(extendInnerSpan > 0.0) { "must be extendInnerSpan($extendInnerSpan) > 0" }
    require(extendOuterSpan > 0.0) { "must be extendOuterSpan($extendOuterSpan) > 0" }
    require(extendDegree >= 0) { "must be extendDegree($extendDegree ) >= 0" }
    require(fillSpan > 0.0) { "must be fillSpan($fillSpan ) > 0" }

    val last = sortedData.last()
    val innerOuterBSpline = last.param.let { Interval(it - extendInnerSpan, it + extendOuterSpan) }
    val (begin, end) = innerOuterBSpline
    val innerPoints = sortedData.filter { it.param >= begin }
    val outerBezier = Interval(extendInnerSpan / (extendOuterSpan + extendInnerSpan), 1.0)
    val outerBSpline = Interval(last.param, end)
    return extend(
        innerPoints,
        innerOuterBSpline,
        outerBezier,
        outerBSpline,
        last.weight,
        extendInnerSpan,
        extendOuterSpan,
        extendDegree,
        fillSpan
    )
}

private fun extend(
    innerPoints: List<WeightedParamPoint>,
    innerOuterBSpline: Interval,
    outerBezier: Interval,
    outerBSpline: Interval,
    weight: Double,
    extendInnerSpan: Double,
    extendOuterSpan: Double,
    extendDegree: Int,
    fillSpan: Double
): List<WeightedParamPoint> {
    val innerData =
        transformParams(innerPoints.map { it.paramPoint }, domain = innerOuterBSpline, range = Interval.Unit)
    val bezier = BezierFitter(extendDegree).fit(innerData).clipout(outerBezier)
    val nSamples = listOf(
        ceil((extendOuterSpan) / fillSpan).toInt(),
        ceil(innerData.size * extendOuterSpan / extendInnerSpan).toInt(),
        2
    ).maxOrNull()!!
    val points = bezier.sample(Sampler(nSamples))
    return transformParams(points, range = outerBSpline).map { it.weighted(weight) }
}
