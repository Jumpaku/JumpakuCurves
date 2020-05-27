package jumpaku.curves.fsc.snap.point

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.GridPointJson


fun Grid.transformToWorld(pointSnapResult: PointSnapResult): Point =
        pointSnapResult.run { transformToWorld(gridPoint, resolution) }

class PointSnapResult(
        val resolution: Int,
        val gridPoint: GridPoint,
        val grade: Grade)

object PointSnapResultJson : JsonConverterBase<PointSnapResult>() {

    override fun toJson(src: PointSnapResult): JsonElement = src.run {
        jsonObject(
                "resolution" to resolution.toJson(),
                "gridPoint" to GridPointJson.toJson(gridPoint),
                "grade" to GradeJson.toJson(grade))
    }

    override fun fromJson(json: JsonElement): PointSnapResult = PointSnapResult(
            json["resolution"].int,
            GridPointJson.fromJson(json["gridPoint"]),
            GradeJson.fromJson(json["grade"].asJsonPrimitive))
}
