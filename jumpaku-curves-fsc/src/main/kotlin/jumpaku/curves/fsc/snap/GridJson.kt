package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson
import jumpaku.curves.core.transform.RotateJson
import jumpaku.curves.core.transform.SimilarityTransformJson

object GridJson : JsonConverterBase<Grid>() {

    override fun toJson(src: Grid): JsonElement = jsonObject(
        "baseSpacingInWorld" to src.baseSpacingInWorld.toJson(),
        "magnification" to src.magnification.toJson(),
        "gridToWorld" to SimilarityTransformJson.toJson(src.gridToWorld),
        "baseFuzzinessInWorld" to src.baseFuzzinessInWorld.toJson()
    )

    override fun fromJson(json: JsonElement): Grid = Grid(
        baseSpacingInWorld = json["baseSpacingInWorld"].double,
        magnification = json["magnification"].int,
        gridToWorld = SimilarityTransformJson.fromJson(json["gridToWorld"]),
        baseFuzzinessInWorld = json["baseFuzzinessInWorld"].double
    )
}