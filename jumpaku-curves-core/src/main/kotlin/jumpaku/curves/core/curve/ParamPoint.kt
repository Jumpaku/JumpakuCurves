package jumpaku.curves.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp

data class ParamPoint(val point: Point, val param: Double) : Lerpable<ParamPoint>, ToJson {

    init {
        require(param.isFinite()) { "param($param)" }
    }

    override fun lerp(vararg terms: Pair<Double, ParamPoint>): ParamPoint = ParamPoint(
            point.lerp(*terms.map { (c, p) -> c to p.point }.toTypedArray()),
            param.lerp(*terms.map { (c, p) -> c to p.param }.toTypedArray()))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("point" to point.toJson(), "param" to param.toJson())

    companion object {

        fun now(point: Point): ParamPoint = ParamPoint(point, System.nanoTime() * 1.0e-9)

        fun fromJson(json: JsonElement): ParamPoint = ParamPoint(Point.fromJson(json["point"]), json["param"].double)
    }
}
