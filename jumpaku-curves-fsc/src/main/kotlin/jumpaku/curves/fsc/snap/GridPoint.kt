package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase


data class GridPoint(val x: Long, val y: Long, val z: Long)

object GridPointJson : JsonConverterBase<GridPoint>() {


    override fun toJson(src: GridPoint): JsonElement = src.run {
        jsonObject(
                "x" to x.toJson(),
                "y" to y.toJson(),
                "z" to z.toJson())
    }

    override fun fromJson(json: JsonElement): GridPoint = GridPoint(json["x"].long, json["y"].long, json["z"].long)
}
