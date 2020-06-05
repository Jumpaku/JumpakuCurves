package jumpaku.curves.fsc.fragment

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.fuzzy.GradeJson

object FragmenterJson : JsonConverterBase<Fragmenter>() {

    override fun toJson(src: Fragmenter): JsonElement = src.run {
        jsonObject(
                "necessityThreshold" to GradeJson.toJson(threshold.necessity),
                "possibilityThreshold" to GradeJson.toJson(threshold.possibility),
                "chunkSize" to chunkSize.toJson(),
                "minStayTimeSpan" to minStayTimeSpan.toJson())
    }

    override fun fromJson(json: JsonElement): Fragmenter = Fragmenter(
            Chunk.Threshold(
                    GradeJson.fromJson(json["necessityThreshold"].asJsonPrimitive),
                    GradeJson.fromJson(json["possibilityThreshold"].asJsonPrimitive)),
            json["chunkSize"].int,
            json["minStayTimeSpan"].double)
}