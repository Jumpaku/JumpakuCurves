package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.geom.Point

fun Grid.toWorldPoint(gridPoint: GridPoint, resolution: Int): Point = gridPoint.run {
    localToWorld(resolution)(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))
}

data class GridPoint(val x: Long, val y: Long, val z: Long) : ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "x" to x.toJson(),
            "y" to y.toJson(),
            "z" to z.toJson())

    companion object {

        fun fromJson(json: JsonElement): GridPoint = GridPoint(json["x"].long, json["y"].long, json["z"].long)
    }
}
