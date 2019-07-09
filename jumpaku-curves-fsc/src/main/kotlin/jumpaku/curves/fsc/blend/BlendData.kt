package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.weighted

class BlendData(val front: List<ParamPoint>, val back: List<ParamPoint>, val blended: List<WeightedParamPoint>) : ToJson {

    val aggregated: List<WeightedParamPoint> = ((front + back).map { it.weighted(1.0) } + blended)
            .sortedBy { it.param }

    override fun toJson(): JsonElement = jsonObject(
            "front" to front.map { it.toJson() }.toJsonArray(),
            "back" to back.map { it.toJson() }.toJsonArray(),
            "blended" to blended.map { it.toJson() }.toJsonArray())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): BlendData = BlendData(
                json["front"].array.map { ParamPoint.fromJson(it) },
                json["back"].array.map { ParamPoint.fromJson(it) },
                json["blended"].array.map { WeightedParamPoint.fromJson(it) })
    }
}