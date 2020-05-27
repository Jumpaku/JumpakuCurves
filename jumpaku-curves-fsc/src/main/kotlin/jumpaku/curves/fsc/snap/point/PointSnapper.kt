package jumpaku.curves.fsc.snap.point

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid

interface PointSnapper {

    fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>
}

object PointSnapperJson : JsonConverterBase<PointSnapper>() {

    override fun toJson(src: PointSnapper): JsonElement = src.run {
        when (this) {
            is MFGS -> jsonObject(
                    "type" to "MFGS",
                    "minResolution" to minResolution.toJson(),
                    "maxResolution" to maxResolution.toJson())
            else -> jsonObject("type" to "IFGS")
        }
    }

    override fun fromJson(json: JsonElement): PointSnapper = when (json["type"].string) {
        "MFGS" -> MFGS(json["minResolution"].int, json["maxResolution"].int)
        "IFGS" -> IFGS
        else -> error("invalid PointSnapper type ${json["type"].string}")
    }
}

