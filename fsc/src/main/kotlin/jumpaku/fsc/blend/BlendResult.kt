package jumpaku.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.curve.ParamPoint
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.util.*

data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val data: Option<Array<ParamPoint>>): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement {
        val osmJson = jsonArray(osm.matrix.map { jsonArray(it.map { it.toJson() }) })
        val pathJson = path.map { (type, grade, path) -> jsonObject(
                "type" to type.toString(),
                "grade" to grade.toJson(),
                "pairs" to jsonArray(path.map { (i, j) -> jsonObject("i" to i.toJson(), "j" to j.toJson()) })) }
                .toJson()
        val dataJson = data.map { jsonArray(it.map { it.toJson() }) }.toJson()
        return jsonObject("osm" to osmJson, "path" to pathJson, "data" to dataJson)
    }

    companion object {

        fun fromJson(json: JsonElement): Result<BlendResult> = result {
            val osm = OverlappingMatrix(Array.ofAll(json["osm"].array.map {
                Array.ofAll(it.array.flatMap { Grade.fromJson(it.asJsonPrimitive).value() })
            }))
            val path = Option.fromJson(json["path"]).orThrow()
                    .map {
                OverlappingPath(
                        OverlappingType.valueOf(it["type"].string),
                        Grade.fromJson(it["grade"].asJsonPrimitive).orThrow(),
                        Array.ofAll(it["pairs"].array.map { Tuple2(it["i"].int, it["j"].int) }))
            }
            val data = Option.fromJson(json["data"]) {
                Array.ofAll(it.array.flatMap { ParamPoint.fromJson(it).value() })
            }.orThrow()
            BlendResult(osm, path, data)
        }
    }
}
