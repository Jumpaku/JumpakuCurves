package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.VectorJson

object BezierDerivativeJson : JsonConverterBase<BezierDerivative>() {

    override fun toJson(src: BezierDerivative): JsonElement = src.run {
        jsonObject("controlVectors" to jsonArray(controlVectors.map { VectorJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): BezierDerivative =
            BezierDerivative(json["controlVectors"].array.map { VectorJson.fromJson(it) })
}