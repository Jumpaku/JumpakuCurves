package jumpaku.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.affine.Divisible
import jumpaku.core.affine.Point
import jumpaku.core.affine.divide
import jumpaku.core.affine.point
import jumpaku.core.json.ToJson

data class ParamPoint(val point: Point, val param: Double) : Divisible<ParamPoint>, ToJson {

    override fun divide(t: Double, p: ParamPoint): ParamPoint {
        return ParamPoint(point.divide(t, p.point), param.divide(t, p.param))
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "param" to param.toJson())

    companion object{

        fun now(point: Point): ParamPoint = ParamPoint(point, System.nanoTime() * 1.0e-9)
    }
}

val JsonElement.paramPoint: ParamPoint get() = ParamPoint(this["point"].point, this["param"].double)