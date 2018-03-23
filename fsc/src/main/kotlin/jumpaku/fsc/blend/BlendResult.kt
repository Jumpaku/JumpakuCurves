package jumpaku.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.jsonOption
import jumpaku.core.json.option
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2

data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val blended: Option<BSpline>): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement {
        val osmJson = jsonArray(osm.matrix.map { jsonArray(it.map { it.toJson() }) })
        val pathJson = jsonOption(path.map { (_, grade, path) ->
            jsonObject(
                    "grade" to grade.toJson(),
                    "pairs" to jsonArray(path.map { (i, j) ->
                        jsonObject("i" to i.toJson(), "j" to j.toJson())
                    }))
        })
        val blendedJson = jsonOption(blended.map { it.toJson() })
        return jsonObject(
                "osm" to osmJson,
                "path" to pathJson,
                "blended" to blendedJson)
    }

    companion object {

        fun fromJson(json: JsonElement): Option<BlendResult> = Try.ofSupplier {
            val osm = OverlappingMatrix(Array.ofAll(json["osm"].array.map { Array.ofAll(it.array.flatMap { Grade.fromJson(it.asJsonPrimitive) }) }))
            val path = json["path"].option.map {
                OverlappingPath(
                        osm,
                        Grade.fromJson(it["grade"].asJsonPrimitive).get(),
                        Array.ofAll(it["pairs"].array.map { Tuple2(it["i"].int, it["j"].int) }))
            }
            val blended = json["blended"].option.map { it.bSpline }

            BlendResult(osm, path, blended)
        }.toOption()

        fun fromJsonString(json: String): Option<BlendResult> = json.parseJson().flatMap { fromJson(it) }
    }
}
