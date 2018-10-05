package jumpaku.fsc.identify

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Map
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.hashMap
import jumpaku.core.json.jsonMap
import jumpaku.core.util.Result
import jumpaku.core.util.result
import jumpaku.fsc.identify.reference.Reference

data class IdentifyResult(
        val grades: Map<CurveClass, Grade>,
        val linear: Reference,
        val circular: Reference,
        val elliptic: Reference): ToJson {

    val grade: Grade get() = grades.toStream().map { (_ , m) -> m }.max().get()

    val curveClass: CurveClass get() = grades.maxBy { (_, m) -> m }.get()._1

    init {
        require(grades.nonEmpty()) { "empty grades" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grades" to jsonMap(grades.map { k, v -> Tuple2(k.name.toJson(), v.toJson()) }),
            "linear" to linear.toJson(),
            "circular" to circular.toJson(),
            "elliptic" to elliptic.toJson())

    companion object {

        fun fromJson(json: JsonElement): Result<IdentifyResult> = result {
            IdentifyResult(
                    json["grades"].hashMap.map { c, g ->
                        Tuple2(CurveClass.valueOf(c.string), Grade.fromJson(g.asJsonPrimitive).orThrow())
                    },
                    Reference.fromJson(json["linear"]).orThrow(),
                    Reference.fromJson(json["circular"]).orThrow(),
                    Reference.fromJson(json["elliptic"]).orThrow())
        }
    }
}