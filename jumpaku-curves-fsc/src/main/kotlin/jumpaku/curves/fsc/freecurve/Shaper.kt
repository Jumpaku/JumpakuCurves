package jumpaku.curves.fsc.freecurve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.Open4IdentifierJson

class Shaper(val segmenter: Segmenter = Segmenter(identifier = Open4Identifier(nSamples = 25, nFmps = 15)),
             val smoother: Smoother = Smoother(pruningFactor = 2.0, samplingFactor = 33),
             val sampler: Sampler) {

    sealed class Sampler {

        abstract operator fun invoke(domain: Interval): List<Double>

        class ByFixedNumber(val nSamples: Int) : Sampler() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(nSamples)
        }

        class ByEqualInterval(val samplingSpan: Double) : Sampler() {

            override fun invoke(domain: Interval): List<Double> = domain.sample(samplingSpan)
        }
    }

    fun shape(fsc: BSpline): Triple<List<Double>, SegmentResult, SmoothResult> {
        val ts = sampler(fsc.domain)
        val segmentResult = segmenter.segment(fsc, ts)
        val smoothResult = smoother.smooth(fsc, ts, segmentResult)
        return Triple(ts, segmentResult, smoothResult)
    }
}

object ShaperJson : JsonConverterBase<Shaper>() {

    override fun toJson(src: Shaper): JsonElement = src.run {
        jsonObject(
                "segmenter" to jsonObject(
                        "identifier" to Open4IdentifierJson.toJson(segmenter.identifier)),
                "smoother" to jsonObject(
                        "pruningFactor" to smoother.pruningFactor.toJson(),
                        "samplingFactor" to smoother.samplingFactor.toJson()),
                "sampler" to when (sampler) {
                    is Shaper.Sampler.ByFixedNumber -> jsonObject(
                            "type" to "ByFixedNumber".toJson(), "nSamples" to sampler.nSamples.toJson())
                    is Shaper.Sampler.ByEqualInterval -> jsonObject(
                            "type" to "ByEqualInterval".toJson(), "samplingSpan" to sampler.samplingSpan.toJson())
                })
    }

    override fun fromJson(json: JsonElement): Shaper = Shaper(
            Segmenter(Open4IdentifierJson.fromJson(json["segmenter"]["identifier"])),
            Smoother(json["smoother"]["pruningFactor"].double,
                    json["smoother"]["samplingFactor"].int),
            when (json["sampler"]["type"].string) {
                "ByFixedNumber" -> Shaper.Sampler.ByFixedNumber(json["sampler"]["nSamples"].int)
                "ByEqualInterval" -> Shaper.Sampler.ByEqualInterval(json["sampler"]["samplingSpan"].double)
                else -> error("invalid type ${json["type"].string}")
            })
}