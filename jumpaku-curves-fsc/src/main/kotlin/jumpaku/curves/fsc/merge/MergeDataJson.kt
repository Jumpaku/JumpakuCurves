package jumpaku.curves.fsc.merge

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.fsc.generate.fit.WeightedParamPointJson

object MergeDataJson : JsonConverterBase<MergeData>() {
    override fun toJson(src: MergeData): JsonElement = src.run {
        jsonObject(
                "grade" to GradeJson.toJson(grade),
                "front" to front.map { ParamPointJson.toJson(it) }.toJsonArray(),
                "back" to back.map { ParamPointJson.toJson(it) }.toJsonArray(),
                "merged" to merged.map { WeightedParamPointJson.toJson(it) }.toJsonArray())
    }

    override fun fromJson(json: JsonElement): MergeData = MergeData(
            GradeJson.fromJson(json["grade"].asJsonPrimitive),
            json["front"].array.map { ParamPointJson.fromJson(it) },
            json["back"].array.map { ParamPointJson.fromJson(it) },
            json["merged"].array.map { WeightedParamPointJson.fromJson(it) })
}