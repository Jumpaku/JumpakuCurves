package jumpaku.curves.fsc.generate.fit

import jumpaku.curves.core.curve.ParamPoint
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

