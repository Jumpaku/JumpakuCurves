package jumpaku.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.ParamPoint
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.jsonOption
import jumpaku.core.json.option
import jumpaku.core.util.component1
import jumpaku.core.util.component2

data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val data: Option<Array<ParamPoint>>): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement {
        val osmJson = jsonArray(osm.matrix.map { jsonArray(it.map { it.toJson() }) })
        val pathJson = jsonOption(path.map { (type, grade, path) -> jsonObject(
                "type" to type.toString(),
                "grade" to grade.toJson(),
                "pairs" to jsonArray(path.map { (i, j) -> jsonObject("i" to i.toJson(), "j" to j.toJson()) })) })
        val dataJson = jsonOption(data.map { jsonArray(it.map { it.toJson() }) })
        return jsonObject("osm" to osmJson, "path" to pathJson, "data" to dataJson)
    }

    companion object {

        fun fromJson(json: JsonElement): Option<BlendResult> = Try.ofSupplier {
            val osm = OverlappingMatrix(Array.ofAll(json["osm"].array.map { Array.ofAll(it.array.flatMap { Grade.fromJson(it.asJsonPrimitive) }) }))
            val path = json["path"].option.map {
                OverlappingPath(
                        OverlappingType.valueOf(it["type"].string),
                        Grade.fromJson(it["grade"].asJsonPrimitive).get(),
                        Array.ofAll(it["pairs"].array.map { Tuple2(it["i"].int, it["j"].int) }))
            }
            val data = json["data"].option.map { Array.ofAll(it.array.flatMap { ParamPoint.fromJson(it) }) }
            BlendResult(osm, path, data)
        }.toOption()
    }
}
