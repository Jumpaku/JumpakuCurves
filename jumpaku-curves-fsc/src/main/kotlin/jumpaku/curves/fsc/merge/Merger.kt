package jumpaku.curves.fsc.merge

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import java.util.*
import kotlin.math.abs

class Merger(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val extendDegree: Int = 2,
        val extendInnerSpan: Double = knotSpan * 2,
        val extendOuterSpan: Double = knotSpan * 2,
        val samplingSpan: Double = 0.01,
        val overlapThreshold: Grade = Grade.FALSE,
        val mergeRate: Double = 0.5,
        val bandWidth: Double = 0.01,
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(
                velocityCoefficient = 0.86,
                accelerationCoefficient = 0.77)
) : ToJson {

    init {
        require(degree >= 0)
        require(knotSpan > 0.0)
        require(extendDegree >= 0)
        require(extendInnerSpan > 0.0)
        require(extendOuterSpan > 0.0)
        require(samplingSpan > 0.0)
        require(mergeRate in 0.0..1.0)
        require(bandWidth > 0.0)
    }

    val detector: OverlapDetector = OverlapDetector(overlapThreshold)

    fun tryMerge(existing: BSpline, overlapping: BSpline): Option<BSpline> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        return detector.detect(existSamples, overlapSamples).map { overlapState ->
            val data = MergeData.parameterize(existSamples, overlapSamples, mergeRate, overlapState)
            generate(data)
        }
    }

    fun generate(mergeData: MergeData): BSpline {
        val data = mergeData.aggregated
                .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { weightByKde(it, bandWidth) }

        fun shouldRemove(knot: Double): Boolean = mergeData.run {
            (frontInterval.map { it.end..mergeInterval.begin } + backInterval.map { mergeInterval.end..it.begin })
                    .any { knot in it }
        }
        val extendedDomain = Interval(data.first().param, data.last().param)
        val removedKnots = LinkedList<Knot>()
        val remainedKnots = LinkedList<Knot>()
        KnotVector.clamped(extendedDomain, degree, knotSpan).knots.forEach { knot ->
            if (shouldRemove(knot.value)) removedKnots.add(knot)
            else remainedKnots.add(knot)
        }
        val knotVector = KnotVector(degree, remainedKnots)
        return Generator.generate(data, knotVector, fuzzifier)
                .run { restrict(mergeData.domain) }
                .let { s -> removedKnots.fold(s) { inserted, (v, m) -> inserted.insertKnot(v, m) } }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "degree" to degree.toJson(),
            "knotSpan" to knotSpan.toJson(),
            "extendDegree" to extendDegree.toJson(),
            "extendInnerSpan" to extendInnerSpan.toJson(),
            "extendOuterSpan" to extendOuterSpan.toJson(),
            "samplingSpan" to samplingSpan.toJson(),
            "overlapThreshold" to overlapThreshold.toJson(),
            "mergeRate" to mergeRate.toJson(),
            "bandWidth" to bandWidth.toJson(),
            "fuzzifier" to fuzzifier.toJson())

    companion object {

        fun weightByKde(paramPoints: List<WeightedParamPoint>, bandWidth: Double): List<WeightedParamPoint> {
            val n = paramPoints.size
            fun kernel(t: Double): Double = if (abs(t) > 1) 0.0 else 15 * (1 - t * t) * (1 - t * t) / 16
            fun density(t: Double): Double = paramPoints.sumByDouble { kernel((t - it.param) / bandWidth) } / n
            return paramPoints.map { it.run { copy(weight = weight / density(it.param)) } }
        }

        fun derive(
                generator: Generator,
                samplingSpan: Double = 0.01,
                overlapThreshold: Grade = Grade.FALSE,
                mergeRate: Double = 0.5,
                bandWidth: Double = 0.01
        ) : Merger = Merger(
                degree = generator.degree,
                knotSpan = generator.knotSpan,
                extendDegree = generator.extendDegree,
                extendInnerSpan = generator.extendInnerSpan,
                extendOuterSpan = generator.extendOuterSpan,
                samplingSpan = samplingSpan,
                overlapThreshold = overlapThreshold,
                mergeRate = mergeRate,
                bandWidth = bandWidth,
                fuzzifier = generator.fuzzifier)

        fun fromJson(json: JsonElement): Merger = Merger(
                degree = json["degree"].int,
                knotSpan = json["knotSpan"].double,
                extendDegree = json["extendDegree"].int,
                extendInnerSpan = json["extendInnerSpan"].double,
                extendOuterSpan = json["extendOuterSpan"].double,
                samplingSpan = json["samplingSpan"].double,
                mergeRate = json["mergeRate"].double,
                overlapThreshold = Grade.fromJson(json["overlapThreshold"].asJsonPrimitive),
                bandWidth = json["bandWidth"].double,
                fuzzifier = Fuzzifier.fromJson(json["fuzzifier"]))
    }
}