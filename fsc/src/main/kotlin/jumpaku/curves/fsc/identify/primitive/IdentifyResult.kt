package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.json.jsonMap
import jumpaku.curves.core.json.map
import jumpaku.curves.core.util.*
import jumpaku.curves.fsc.identify.primitive.reference.Reference

class IdentifyResult(
        grades: Map<CurveClass, Grade>,
        val linear: Reference,
        val circular: Reference,
        val elliptic: Reference): ToJson {

    val grades: Map<CurveClass, Grade> = grades.toMap()

    val grade: Grade get() = grades.asVavr().toStream().map { (_ , m) -> m }.max().get()

    val curveClass: CurveClass get() = grades.maxBy { (_, m) -> m }!!.key

    init {
        require(grades.isNotEmpty()) { "empty grades" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grades" to jsonMap(grades.map { (k, v) -> k.name.toJson() to v.toJson() }.toMap()),
            "linear" to linear.toJson(),
            "circular" to circular.toJson(),
            "elliptic" to elliptic.toJson())

    companion object {

        fun fromJson(json: JsonElement): IdentifyResult = IdentifyResult(
                json["grades"].map.map { (c, g) -> CurveClass.valueOf(c.string) to Grade.fromJson(g.asJsonPrimitive) }.toMap(),
                Reference.fromJson(json["linear"]),
                Reference.fromJson(json["circular"]),
                Reference.fromJson(json["elliptic"]))
    }
}