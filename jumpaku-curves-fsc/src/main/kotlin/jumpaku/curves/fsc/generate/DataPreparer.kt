package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.curve.weighted
import jumpaku.curves.core.geom.middle
import jumpaku.curves.fsc.generate.fit.BezierFitter
import org.apache.commons.math3.util.FastMath


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
        val fillSpan: Double,
        val extendInnerSpan: Double,
        val extendOuterSpan: Double,
        val extendDegree: Int) : ToJson {

    init {
        require(extendInnerSpan > 0.0) { "must be extendInnerSpan($extendInnerSpan) > 0" }
        require(extendOuterSpan > 0.0) { "must be extendOuterSpan($extendOuterSpan) > 0" }
    }

    fun prepare(crispData: List<WeightedParamPoint>): List<WeightedParamPoint> {
        require(crispData.size >= 2) { "data.size == ${crispData.size}, too few data" }

        return crispData.sortedBy(WeightedParamPoint::param)
                .let { fill(it) }
                .let { extendFront(it) }
                .let { extendBack(it) }
    }

    fun fill(sortedData: List<WeightedParamPoint>): List<WeightedParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        return sortedData.drop(1).fold(mutableListOf(sortedData.first())) { filled, (nextP, nextW) ->
            val (prevP, prevW) = filled.last()
            val n = FastMath.ceil((nextP.param - prevP.param) / fillSpan)
            filled += (1..n.toInt()).map { prevP.lerp(it / n, nextP).weighted(prevW.middle(nextW)) }
            filled
        }
    }

    fun extendFront(sortedData: List<WeightedParamPoint>): List<WeightedParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        val first = sortedData.first()
        val innerOuterBSpline = first.param.let { Interval(it - extendOuterSpan, it + extendInnerSpan) }
        val (begin, end) = innerOuterBSpline
        val innerPoints = sortedData.filter { it.param <= end }
        val outerBezier = Interval(0.0, extendOuterSpan / (extendOuterSpan + extendInnerSpan))
        val outerBSpline = Interval(begin, first.param)
        val outerData = extend(innerPoints, innerOuterBSpline, outerBezier, outerBSpline, first.weight)
        return outerData + sortedData
    }

    fun extendBack(sortedData: List<WeightedParamPoint>): List<WeightedParamPoint> {
        require(sortedData.size >= 2) { "data.size == ${sortedData.size}, too few data" }

        val last = sortedData.last()
        val innerOuterBSpline = last.param.let { Interval(it - extendInnerSpan, it + extendOuterSpan) }
        val (begin, end) = innerOuterBSpline
        val innerPoints = sortedData.filter { it.param >= begin }
        val outerBezier = Interval(extendInnerSpan / (extendOuterSpan + extendInnerSpan), 1.0)
        val outerBSpline = Interval(last.param, end)
        val outerData = extend(innerPoints, innerOuterBSpline, outerBezier, outerBSpline, last.weight)
        return sortedData + outerData
    }

    private fun extend(
            innerPoints: List<WeightedParamPoint>,
            innerOuterBSpline: Interval,
            outerBezier: Interval,
            outerBSpline: Interval,
            weight: Double
    ): List<WeightedParamPoint> {
        val innerData = transformParams(innerPoints.map { it.paramPoint }, domain = innerOuterBSpline, range = Interval.ZERO_ONE)
        val bezier = BezierFitter(extendDegree).fit(innerData).restrict(outerBezier)
        val points = bezier.sample(Math.ceil(innerData.size * extendOuterSpan / extendInnerSpan).toInt())
        return transformParams(points, range = outerBSpline).map { it.weighted(weight) }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "fillSpan" to fillSpan.toJson(),
            "extendInnerSpan" to extendInnerSpan.toJson(),
            "extendOuterSpan" to extendOuterSpan.toJson(),
            "extendDegree" to extendDegree)

    companion object {

        fun fromJson(json: JsonElement): DataPreparer = DataPreparer(
                json["fillSpan"].double,
                json["extendInnerSpan"].double,
                json["extendOuterSpan"].double,
                json["extendDegree"].int)
    }
}