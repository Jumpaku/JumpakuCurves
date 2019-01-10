package jumpaku.fsc.snap.point

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.toWorldPoint


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
                GridPoint.fromJson(json["gridPoint"]).orThrow(),
                Grade.fromJson(json["grade"].asJsonPrimitive))
    }
}
