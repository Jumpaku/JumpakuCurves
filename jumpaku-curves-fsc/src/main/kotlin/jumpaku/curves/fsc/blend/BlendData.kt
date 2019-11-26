package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted

class BlendData(val grade: Grade, val front: List<ParamPoint>, val back: List<ParamPoint>, val blended: List<WeightedParamPoint>) : ToJson {

    val aggregated: List<WeightedParamPoint> = ((front + back).map { it.weighted(1.0) } + blended)
            .sortedBy { it.param }

    override fun toJson(): JsonElement = jsonObject(
            "grade" to grade.toJson(),
            "front" to front.map { it.toJson() }.toJsonArray(),
            "back" to back.map { it.toJson() }.toJsonArray(),
            "blended" to blended.map { it.toJson() }.toJsonArray())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): BlendData = BlendData(
                Grade.fromJson(json["grade"].asJsonPrimitive),
                json["front"].array.map { ParamPoint.fromJson(it) },
                json["back"].array.map { ParamPoint.fromJson(it) },
                json["blended"].array.map { WeightedParamPoint.fromJson(it) })
    }
}