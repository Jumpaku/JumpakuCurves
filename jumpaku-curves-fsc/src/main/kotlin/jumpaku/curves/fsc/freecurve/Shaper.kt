package jumpaku.curves.fsc.freecurve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.identify.primitive.Open4Identifier

class Shaper(val segmenter: Segmenter = Segmenter(identifier = Open4Identifier(nSamples = 25, nFmps = 15)),
             val smoother: Smoother = Smoother(pruningFactor = 2.0, samplingFactor = 33),
             val sampleMethod: SampleMethod): ToJson {

    sealed class SampleMethod: ToJson {

        abstract operator fun invoke(domain: Interval): List<Double>

        override fun toJson(): JsonElement = when(this) {
            is ByFixedNumber ->
                jsonObject("type" to "ByFixedNumber".toJson(), "nSamples" to nSamples.toJson())
            is ByEqualInterval ->
                jsonObject("type" to "ByEqualInterval".toJson(), "samplingSpan" to samplingSpan.toJson())
        }

        override fun toString(): String = toJsonString()

        companion object {

            fun fromJson(json: JsonElement): SampleMethod = when(json["type"].string) {
                "ByFixedNumber" -> ByFixedNumber(json["nSamples"].int)
                "ByEqualInterval" -> ByEqualInterval(json["samplingSpan"].double)
                else -> error("invalid type ${json["type"].string}")
            }
        }

        class ByFixedNumber(val nSamples: Int): SampleMethod() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(nSamples)
        }

        class ByEqualInterval(val samplingSpan: Double): SampleMethod() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(samplingSpan)
        }
    }

    fun shape(fsc: BSpline): Triple<List<Double>, SegmentResult, SmoothResult> {
        val ts = sampleMethod(fsc.domain)
        val segmentResult = segmenter.segment(fsc, ts)
        val smoothResult = smoother.smooth(fsc, ts, segmentResult)
        return Triple(ts, segmentResult, smoothResult)
    }

    override fun toJson(): JsonElement = jsonObject(
            "segmenter" to segmenter.toJson(),
            "smoother" to smoother.toJson(),
            "sampleMethod" to sampleMethod.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): Shaper = Shaper(
                Segmenter.fromJson(json["segmenter"]),
                Smoother.fromJson(json["smoother"]),
                SampleMethod.fromJson(json["sampleMethod"]))
    }

}