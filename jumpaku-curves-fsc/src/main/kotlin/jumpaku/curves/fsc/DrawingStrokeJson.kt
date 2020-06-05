package jumpaku.curves.fsc

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.ParamPointJson

object DrawingStrokeJson : JsonConverterBase<DrawingStroke>() {
    override fun toJson(src: DrawingStroke): JsonElement = src.run {
        jsonObject("paramPoints" to jsonArray(inputData.map { ParamPointJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): DrawingStroke =
            DrawingStroke(json["paramPoints"].array.map { ParamPointJson.fromJson(it) })

}