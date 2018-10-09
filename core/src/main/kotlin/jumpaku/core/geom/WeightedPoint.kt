package jumpaku.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result

fun Point.weighted(weight: Double = 1.0): WeightedPoint = WeightedPoint(this, weight)

data class WeightedPoint(val point: Point, val weight: Double = 1.0): Divisible<WeightedPoint>, ToJson {

    override fun divide(t: Double, p: WeightedPoint): WeightedPoint {
        val w = weight.divide(t, p.weight)
        return WeightedPoint(point.divide(t * p.weight / w, p.point), w)
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "weight" to weight.toJson())

    companion object {

        fun fromJson(json: JsonElement): Result<WeightedPoint> = result {
            WeightedPoint(Point.fromJson(json["point"]).orThrow(), json["weight"].double)
        }
    }
}

