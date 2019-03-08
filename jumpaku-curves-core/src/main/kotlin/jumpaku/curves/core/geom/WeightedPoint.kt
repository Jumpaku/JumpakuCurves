package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson

fun Point.weighted(weight: Double = 1.0): WeightedPoint = WeightedPoint(this, weight)

data class WeightedPoint(val point: Point, val weight: Double = 1.0): Lerpable<WeightedPoint>, ToJson {

    override fun lerp(vararg terms: Pair<Double, WeightedPoint>): WeightedPoint {
        val w = weight.lerp(*terms.map { (c, wp) -> c to wp.weight }.toTypedArray())
        val p = point.lerp(*terms.map { (c, wp) -> (c*wp.weight/w) to wp.point }.toTypedArray())
        return WeightedPoint(p, w)
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "weight" to weight.toJson())

    companion object {

        fun fromJson(json: JsonElement): WeightedPoint =
            WeightedPoint(Point.fromJson(json["point"]), json["weight"].double)
    }
}

