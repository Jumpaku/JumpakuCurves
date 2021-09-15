package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.transform.SimilarityTransformJson

object GridJson : JsonConverterBase<Grid>() {

    override fun toJson(src: Grid): JsonElement = jsonObject(
        "magnification" to src.magnification.toJson(),
        "baseGridToWorld" to SimilarityTransformJson.toJson(src.baseGridToWorld),
        "baseFuzzinessInWorld" to src.baseFuzzinessInWorld.toJson()
    )

    override fun fromJson(json: JsonElement): Grid = Grid(
        magnification = json["magnification"].int,
        baseGridToWorld = SimilarityTransformJson.fromJson(json["baseGridToWorld"]),
        baseFuzzinessInWorld = json["baseFuzzinessInWorld"].double
    )
}