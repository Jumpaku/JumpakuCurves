package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.fsc.generate.FuzzifierJson

object BlenderJson : JsonConverterBase<Blender>() {

    override fun toJson(src: Blender): JsonElement = src.run {
        jsonObject(
            "degree" to degree.toJson(),
            "knotSpan" to knotSpan.toJson(),
            "extendDegree" to extendDegree.toJson(),
            "extendInnerSpan" to extendInnerSpan.toJson(),
            "extendOuterSpan" to extendOuterSpan.toJson(),
            "samplingSpan" to samplingSpan.toJson(),
            "overlapThreshold" to GradeJson.toJson(overlapThreshold),
            "blendRate" to blendRate.toJson(),
            "bandWidth" to bandWidth.toJson(),
            "fuzzifier" to FuzzifierJson.toJson(fuzzifier)
        )
    }

    override fun fromJson(json: JsonElement): Blender = Blender(
        degree = json["degree"].int,
        knotSpan = json["knotSpan"].double,
        extendDegree = json["extendDegree"].int,
        extendInnerSpan = json["extendInnerSpan"].double,
        extendOuterSpan = json["extendOuterSpan"].double,
        samplingSpan = json["samplingSpan"].double,
        overlapThreshold = GradeJson.fromJson(json["overlapThreshold"].asJsonPrimitive),
        blendRate = json["blendRate"].double,
        bandWidth = json["bandWidth"].double,
        fuzzifier = FuzzifierJson.fromJson(json["fuzzifier"])
    )
}