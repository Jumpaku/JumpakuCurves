package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson
import jumpaku.curves.core.transform.RotateJson

object GridJson : JsonConverterBase<Grid>() {

    override fun toJson(src: Grid): JsonElement = src.run {
        jsonObject(
                "baseSpacing" to baseSpacing.toJson(),
                "magnification" to magnification.toJson(),
                "origin" to PointJson.toJson(origin),
                "rotation" to RotateJson.toJson(rotation),
                "baseFuzziness" to baseFuzziness.toJson())
    }

    override fun fromJson(json: JsonElement): Grid = Grid(
            baseSpacing = json["baseSpacing"].double,
            magnification = json["magnification"].int,
            origin = PointJson.fromJson(json["origin"]),
            rotation = RotateJson.fromJson(json["rotation"]),
            baseFuzziness = json["baseFuzziness"].double)
}