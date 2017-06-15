package org.jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.control.Option
import org.jumpaku.core.json.prettyGson


data class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint> {

    override fun divide(t: Double, p: WeightedPoint): WeightedPoint {
        val w = weight.divide(t, p.weight)
        return WeightedPoint(point.divide(t * p.weight / w, p.point), w)
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): WeightedPointJson = WeightedPointJson(this)
}

data class WeightedPointJson(private val point: PointJson, private val weight: Double) {

    constructor(weightedPoint: WeightedPoint) : this(weightedPoint.point.json(), weightedPoint.weight)

    fun weightedPoint() = WeightedPoint(point.point(), weight)
}
