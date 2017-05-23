package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.control.Option
import org.jumpaku.json.prettyGson


data class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint> {

    override fun divide(t: Double, wp: WeightedPoint): WeightedPoint {
        val w = (1 - t) * weight + t * wp.weight
        return WeightedPoint(point.divide(t * wp.weight / w, wp.point), w)
    }

    override fun toString(): String = WeightedPointJson.toJson(this)
}

data class WeightedPointJson(val point: PointJson, val weight: Double) {

    companion object {

        fun toJson(wp: WeightedPoint): String = prettyGson.toJson(WeightedPointJson(
                wp.point.run { PointJson(x, y, z, r) }, wp.weight))

        fun fromJson(json: String): Option<WeightedPoint> {
            return try {
                val (p, w) = prettyGson.fromJson<WeightedPointJson>(json)
                Option(WeightedPoint(Point.xyzr(p.x, p.y, p.z, p.r), w))
            } catch(e: Exception) {
                None()
            }
        }
    }
}
