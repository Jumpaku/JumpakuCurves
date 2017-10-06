package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.core.affine.Point
import jumpaku.core.json.ToJson

data class GridPoint(val x: Long, val y: Long, val z: Long, val grid: Grid): ToJson {

    fun toCrispPoint(): Point = grid.localToWorld(Point(x.toDouble(), y.toDouble(), z.toDouble()))

    fun toFuzzyPoint(): Point = toCrispPoint().copy(r = grid.fuzziness)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "x" to x.toJson(),
            "y" to y.toJson(),
            "z" to z.toJson(),
            "grid" to grid.toJson())
}

val JsonElement.gridPoint: GridPoint
    get() = GridPoint(
            this["x"].long, this["y"].long, this["z"].long, this["grid"].grid)