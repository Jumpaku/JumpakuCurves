package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object GeneratorJson : JsonConverterBase<Generator>() {
    override fun toJson(src: Generator): JsonElement = src.run {
        jsonObject(
                "degree" to degree.toJson(),
                "knotSpan" to knotSpan.toJson(),
                "fillSpan" to fillSpan.toJson(),
                "extendInnerSpan" to extendInnerSpan.toJson(),
                "extendOuterSpan" to extendOuterSpan.toJson(),
                "extendDegree" to extendDegree.toJson(),
                "fuzzifier" to FuzzifierJson.toJson(fuzzifier))
    }

    override fun fromJson(json: JsonElement): Generator = Generator(
            json["degree"].int,
            json["knotSpan"].double,
            json["fillSpan"].double,
            json["extendInnerSpan"].double,
            json["extendOuterSpan"].double,
            json["extendDegree"].int,
            FuzzifierJson.fromJson(json["fuzzifier"]))
}