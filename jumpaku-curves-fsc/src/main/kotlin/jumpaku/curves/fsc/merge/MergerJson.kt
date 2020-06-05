package jumpaku.curves.fsc.merge

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.fsc.generate.FuzzifierJson

object MergerJson : JsonConverterBase<Merger>() {

    override fun toJson(src: Merger): JsonElement = src.run {
        jsonObject(
                "degree" to degree.toJson(),
                "knotSpan" to knotSpan.toJson(),
                "extendDegree" to extendDegree.toJson(),
                "extendInnerSpan" to extendInnerSpan.toJson(),
                "extendOuterSpan" to extendOuterSpan.toJson(),
                "samplingSpan" to samplingSpan.toJson(),
                "overlapThreshold" to GradeJson.toJson(overlapThreshold),
                "mergeRate" to mergeRate.toJson(),
                "bandWidth" to bandWidth.toJson(),
                "fuzzifier" to FuzzifierJson.toJson(fuzzifier))
    }

    override fun fromJson(json: JsonElement): Merger = Merger(
            degree = json["degree"].int,
            knotSpan = json["knotSpan"].double,
            extendDegree = json["extendDegree"].int,
            extendInnerSpan = json["extendInnerSpan"].double,
            extendOuterSpan = json["extendOuterSpan"].double,
            samplingSpan = json["samplingSpan"].double,
            mergeRate = json["mergeRate"].double,
            overlapThreshold = GradeJson.fromJson(json["overlapThreshold"].asJsonPrimitive),
            bandWidth = json["bandWidth"].double,
            fuzzifier = FuzzifierJson.fromJson(json["fuzzifier"]))

}