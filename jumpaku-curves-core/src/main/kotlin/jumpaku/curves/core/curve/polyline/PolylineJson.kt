package jumpaku.curves.core.curve.polyline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.ParamPointJson

object PolylineJson : JsonConverterBase<Polyline>() {

    override fun toJson(src: Polyline): JsonElement = src.run {
        jsonObject("paramPoints" to jsonArray(paramPoints.map { ParamPointJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): Polyline = Polyline(json["paramPoints"].array.map { ParamPointJson.fromJson(it) })

}