package jumpaku.curves.fsc.snap.point

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.commons.json.ToJson
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.toWorldPoint


class PointSnapResult(
        val resolution: Int,
        val gridPoint: GridPoint,
        val grade: Grade): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "resolution" to resolution.toJson(),
            "gridPoint" to gridPoint.toJson(),
            "grade" to grade.toJson())

    fun worldPoint(grid: Grid): Point = grid.toWorldPoint(gridPoint, resolution)

    companion object {

        fun fromJson(json: JsonElement): PointSnapResult = PointSnapResult(
                json["resolution"].int,
                GridPoint.fromJson(json["gridPoint"]),
                Grade.fromJson(json["grade"].asJsonPrimitive))
    }
}
