package jumpaku.core.affine

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement


data class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint> {

    override fun divide(t: Double, p: WeightedPoint): WeightedPoint {
        val w = weight.divide(t, p.weight)
        return WeightedPoint(point.divide(t * p.weight / w, p.point), w)
    }

    override fun toString(): String = toJson().toString()

    fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "weight" to weight.toJson())
}

val JsonElement.weightedPoint: WeightedPoint get() = WeightedPoint(this["point"].point, this["weight"].double)