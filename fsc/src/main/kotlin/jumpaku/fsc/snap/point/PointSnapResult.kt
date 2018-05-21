package jumpaku.fsc.snap.point

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint

data class PointSnapResult(
        val grid: Grid,
        val gridPoint: GridPoint,
        val worldPoint: Point,
        val grade: Grade): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grid" to grid.toJson(),
            "gridPoint" to gridPoint.toJson(),
            "worldPoint" to worldPoint.toJson(),
            "grade" to grade.toJson())

    companion object {

        fun fromJson(json: JsonElement): Option<PointSnapResult> = Try.ofSupplier {
            PointSnapResult(
                    Grid.fromJson(json["grid"]).get(),
                    GridPoint.fromJson(json["gridPoint"]).get(),
                    Point.fromJson(json["worldPoint"]).get(),
                    Grade.fromJson(json["grade"].asJsonPrimitive).get())
        }.toOption()
    }
}

fun noPointSnap(baseGrid: Grid, cursor: Point): PointSnapResult
        = PointSnapResult(Grid.noGrid(baseGrid), GridPoint(0, 0, 0), cursor.toCrisp(), Grade.TRUE)
