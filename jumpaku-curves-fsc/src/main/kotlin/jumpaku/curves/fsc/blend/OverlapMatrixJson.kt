package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object OverlapMatrixJson : JsonConverterBase<OverlapMatrix>() {
    override fun fromJson(json: JsonElement): OverlapMatrix = OverlapMatrix(
        json["rowSize"].int,
        json["columnSize"].int,
        json["values"].array.flatMap { it.array.map { it.double } }.toDoubleArray()
    )

    override fun toJson(src: OverlapMatrix): JsonElement = jsonObject(
        "rowSize" to src.rowSize,
        "columnSize" to src.columnSize,
        "values" to (0 until src.rowSize).map { i ->
            (0 until src.columnSize).map { j -> src[i, j].value }.toJsonArray()
        }.toJsonArray()
    )

}