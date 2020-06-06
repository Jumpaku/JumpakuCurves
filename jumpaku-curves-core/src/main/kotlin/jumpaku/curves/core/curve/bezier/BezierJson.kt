package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson

object BezierJson : JsonConverterBase<Bezier>() {

    override fun toJson(src: Bezier): JsonElement = src.run {
        jsonObject("controlPoints" to jsonArray(controlPoints.map { PointJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): Bezier = Bezier(json["controlPoints"].array.map { PointJson.fromJson(it) })

}