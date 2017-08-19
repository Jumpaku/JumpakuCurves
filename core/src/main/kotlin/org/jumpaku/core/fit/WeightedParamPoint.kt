package org.jumpaku.core.fit

import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.ParamPointJson
import org.jumpaku.core.json.prettyGson

fun ParamPoint.weighted(weight: Double = 1.0): WeightedParamPoint = WeightedParamPoint(this, weight)

data class WeightedParamPoint(val paramPoint: ParamPoint, val weight: Double = 1.0) {

    constructor(point: Point, param: Double, weight: Double = 1.0) : this(ParamPoint(point, param), weight)

    val point: Point = paramPoint.point

    val param: Double = paramPoint.param

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): WeightedParamPointJson = WeightedParamPointJson(this)
}

data class WeightedParamPointJson(
        val paramPoint: ParamPointJson,
        val weight: Double){

    constructor(weightedParamPoint: WeightedParamPoint) : this(weightedParamPoint.paramPoint.json(), weightedParamPoint.weight)

    fun weightedParamPoint(): WeightedParamPoint = WeightedParamPoint(paramPoint.paramPoint(), weight)
}