package jumpaku.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.geom.Divisible
import jumpaku.core.geom.Point
import jumpaku.core.geom.divide
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result

data class ParamPoint(val point: Point, val param: Double) : Divisible<ParamPoint>, ToJson {

    override fun divide(t: Double, p: ParamPoint): ParamPoint =
            ParamPoint(point.divide(t, p.point), param.divide(t, p.param))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "param" to param.toJson())

    companion object{

        fun now(point: Point): ParamPoint = ParamPoint(point, System.nanoTime() * 1.0e-9)

        fun fromJson(json: JsonElement): Result<ParamPoint> =
                result { ParamPoint(Point.fromJson(json["point"]).orThrow(), json["param"].double) }
    }
}
