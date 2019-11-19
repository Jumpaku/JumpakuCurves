package jumpaku.curves.fsc.snap.point

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid

abstract class PointSnapper : ToJson {

    abstract fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>

    override fun toJson(): JsonElement = when (this) {
        is MFGS -> jsonObject(
                "type" to "MFGS",
                "minResolution" to minResolution.toJson(),
                "maxResolution" to maxResolution.toJson())
        else -> jsonObject("type" to "IFGS")
    }

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): PointSnapper = when (json["type"].string) {
            "MFGS" -> MFGS(json["minResolution"].int, json["maxResolution"].int)
            //"FGS" -> IFGS()
            else -> error("invalid PointSnapper type ${json["type"].string}")
        }
    }
}

