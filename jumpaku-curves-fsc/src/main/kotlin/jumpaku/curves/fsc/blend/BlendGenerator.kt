package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import java.util.*
import kotlin.math.abs

class BlendGenerator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val bandWidth: Double = 0.01,
        val extendInnerSpan: Double = knotSpan * 2,
        val extendOuterSpan: Double = knotSpan * 2,
        val extendDegree: Int = 2,
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(0.86, 0.77)
) : ToJson {

    constructor(generator: Generator, bandWidth: Double) : this(
            generator.degree,
            generator.knotSpan,
            bandWidth,
            generator.extendInnerSpan,
            generator.extendOuterSpan,
            generator.degree,
            generator.fuzzifier)

    init {
        require(degree >= 0)
        require(knotSpan > 0.0)
        require(bandWidth > 0.0)
        require(extendInnerSpan > 0.0)
        require(extendOuterSpan > 0.0)
        require(extendDegree >= 0)
    }

    fun kernelDensityEstimate(paramPoints: List<WeightedParamPoint>, bandWidth: Double): List<WeightedParamPoint> {
        val n = paramPoints.size
        fun kernel(t: Double): Double = if (abs(t) > 1) 0.0 else 15 * (1 - t * t) * (1 - t * t) / 16
        fun density(t: Double): Double = paramPoints.sumByDouble { kernel((t - it.param) / bandWidth) } / n
        return paramPoints.map { it.run { copy(weight = weight / density(it.param)) } }
    }

    fun generate(blendData: BlendData): BSpline {
        val data = blendData.aggregated
        val domain = Interval(data.first().param, data.last().param)
        val extended = data
                .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { kernelDensityEstimate(it, bandWidth) }
        val extendedDomain = Interval(extended.first().param, extended.last().param)
        val removedKnots = LinkedList<Knot>()
        val knotVector = KnotVector.clamped(extendedDomain, degree, knotSpan).run {
            val front = blendData.front
            val back = blendData.back
            val blendBegin = blendData.blended.first().param
            val blendEnd = blendData.blended.last().param
            val remainedKnots = LinkedList<Knot>()
            fun shouldRemove(knot: Knot): Boolean =
                    (front.lastOrNull()?.let { knot.value in it.param..blendBegin } ?: false) ||
                            (back.firstOrNull()?.let { knot.value in blendEnd..it.param } ?: false)
            knots.forEach { knot ->
                if (shouldRemove(knot)) removedKnots.add(knot)
                else remainedKnots.add(knot)
            }
            KnotVector(degree, remainedKnots)
        }
        return Generator.generate(extended, knotVector, fuzzifier)
                .run { restrict(domain) }
                .let { s -> removedKnots.fold(s) { inserted, (v, m) -> inserted.insertKnot(v, m) } }
    }


    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "degree" to degree.toJson(),
            "knotSpan" to knotSpan.toJson(),
            "bandWidth" to bandWidth.toJson(),
            "extendInnerSpan" to extendInnerSpan.toJson(),
            "extendOuterSpan" to extendOuterSpan.toJson(),
            "extendDegree" to extendDegree.toJson(),
            "fuzzifier" to fuzzifier.toJson())

    companion object {

        fun fromJson(json: JsonElement): BlendGenerator = BlendGenerator(
                json["degree"].int,
                json["knotSpan"].double,
                json["bandWidth"].double,
                json["extendInnerSpan"].double,
                json["extendOuterSpan"].double,
                json["extendDegree"].int,
                Fuzzifier.fromJson(json["fuzzifier"]))
    }
}