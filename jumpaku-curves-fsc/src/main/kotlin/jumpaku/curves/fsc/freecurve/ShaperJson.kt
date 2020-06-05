package jumpaku.curves.fsc.freecurve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.fsc.identify.primitive.Open4IdentifierJson

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