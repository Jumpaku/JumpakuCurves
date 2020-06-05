package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.WeightedPointJson

object RationalBezierJson : JsonConverterBase<RationalBezier>() {

    override fun toJson(src: RationalBezier): JsonElement = src.run {
        jsonObject(
                "weightedControlPoints" to jsonArray(weightedControlPoints.map { WeightedPointJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): RationalBezier =
            RationalBezier(json["weightedControlPoints"].array.map { WeightedPointJson.fromJson(it) })

}