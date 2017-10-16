package jumpaku.core.fit

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.affine.Point
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.paramPoint

fun ParamPoint.weighted(weight: Double = 1.0): WeightedParamPoint = WeightedParamPoint(this, weight)

data class WeightedParamPoint(val paramPoint: ParamPoint, val weight: Double = 1.0) {

    constructor(point: Point, param: Double, weight: Double = 1.0) : this(ParamPoint(point, param), weight)

    val point: Point = paramPoint.point

    val param: Double = paramPoint.param

    override fun toString(): String = toJson().toString()

    fun toJson(): JsonElement = jsonObject("paramPoint" to paramPoint.toJson(), "weight" to weight.toJson())
}

val JsonElement.weightedParamPoint: WeightedParamPoint get() = WeightedParamPoint(
        this["paramPoint"].paramPoint, this["weight"].double)