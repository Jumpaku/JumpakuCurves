package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.control.Option
import org.jumpaku.json.prettyGson


data class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint> {

    override fun divide(t: Double, p: WeightedPoint): WeightedPoint {
        val w = (1 - t) * weight + t * p.weight
        return WeightedPoint(point.divide(t * p.weight / w, p.point), w)
    }

    override fun toString(): String = WeightedPointJson.toJson(this)
}

data class WeightedPointJson(private val point: PointJson, private val weight: Double) {

    fun weightedPoint() = WeightedPoint(point.point(), weight)

    companion object {

        fun toJson(wp: WeightedPoint): String = prettyGson.toJson(WeightedPointJson(
                wp.point.run { PointJson(x, y, z, r) }, wp.weight))

        fun fromJson(json: String): Option<WeightedPoint> {
            return try {
                Option(prettyGson.fromJson<WeightedPointJson>(json).weightedPoint())
            } catch(e: Exception) {
                None()
            }
        }
    }
}
