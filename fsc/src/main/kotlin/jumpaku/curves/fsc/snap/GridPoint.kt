package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result

fun Grid.toWorldPoint(gridPoint: GridPoint, resolution: Int): Point = gridPoint.run {
    localToWorld(resolution)(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))
}

data class GridPoint(val x: Long, val y: Long, val z: Long): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "x" to x.toJson(),
            "y" to y.toJson(),
            "z" to z.toJson())

    companion object {

        fun fromJson(json: JsonElement): Result<GridPoint> = result {
            GridPoint(json["x"].long, json["y"].long, json["z"].long)
        }
    }
}
