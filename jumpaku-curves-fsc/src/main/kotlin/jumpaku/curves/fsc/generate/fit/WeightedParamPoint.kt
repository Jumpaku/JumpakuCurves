package jumpaku.curves.fsc.generate.fit

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.geom.Point

fun ParamPoint.weighted(weight: Double = 1.0): WeightedParamPoint = WeightedParamPoint(this, weight)

data class WeightedParamPoint(val paramPoint: ParamPoint, val weight: Double = 1.0) {

    constructor(point: Point, param: Double, weight: Double = 1.0) : this(ParamPoint(point, param), weight)

    init {
        require(weight.isFinite()) { "weight($weight)" }
    }

    val point: Point = paramPoint.point

    val param: Double = paramPoint.param

    override fun toString(): String = "WeightedParamPoint(paramPoint=$paramPoint,weight=$weight)"
}

object WeightedParamPointJson : JsonConverterBase<WeightedParamPoint>() {
    override fun toJson(src: WeightedParamPoint): JsonElement = src.run {
        jsonObject("paramPoint" to ParamPointJson.toJson(paramPoint), "weight" to weight.toJson())

    }

    override fun fromJson(json: JsonElement): WeightedParamPoint =
            WeightedParamPoint(ParamPointJson.fromJson(json["paramPoint"]), json["weight"].double)
}
