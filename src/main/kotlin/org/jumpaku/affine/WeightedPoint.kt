package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import org.jumpaku.json.prettyGson


class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint> {

    operator fun component1(): Point = point

    operator fun component2(): Double = weight

    override fun divide(t: Double, wp: WeightedPoint): WeightedPoint {
        val w = (1 - t) * weight + t * wp.weight
        return WeightedPoint(point.divide(t * wp.weight / w, wp.point), w)
    }

    override fun toString(): String = toJson(this)

    companion object {

        data class JsonWeightedPoint(val point: Point.Companion.JsonPoint, val weight: Double)

        fun toJson(wp: WeightedPoint): String = prettyGson.toJson(JsonWeightedPoint(
                Point.Companion.JsonPoint(wp.point.x, wp.point.y, wp.point.z, wp.point.r), wp.weight))

        fun fromJson(json: String): WeightedPoint? {
            return try {
                val (p, w) = prettyGson.fromJson<JsonWeightedPoint>(json)
                WeightedPoint(Point.xyzr(p.x, p.y, p.z, p.r), w)
            } catch(e: Exception) {
                when (e) {
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}