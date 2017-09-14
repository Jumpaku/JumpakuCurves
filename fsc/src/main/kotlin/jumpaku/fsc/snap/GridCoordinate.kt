package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.affine.Point
import jumpaku.core.json.ToJson

data class GridCoordinate(val x: Int, val y: Int, val z: Int, val grid: Grid): ToJson {
    val point: Point = Point(x.toDouble(), y.toDouble(), z.toDouble())
            .transform(grid.rotation.andScale(grid.gridSpacing).andTranslateTo(grid.origin))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "x" to x.toJson(),
            "y" to y.toJson(),
            "z" to z.toJson(),
            "grid" to grid.toJson())
}

val JsonElement.gridCoordinate: GridCoordinate
    get() = GridCoordinate(
            this["x"].int, this["y"].int, this["z"].int, this["grid"].grid)