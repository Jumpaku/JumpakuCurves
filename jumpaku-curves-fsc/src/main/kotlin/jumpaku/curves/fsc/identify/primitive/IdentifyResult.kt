package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.commons.json.jsonMap
import jumpaku.commons.json.map
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.maxBy
import kotlin.collections.toMap

class IdentifyResult(
        grades: Map<CurveClass, Grade>,
        val linear: Reference,
        val circular: Reference,
        val elliptic: Reference): ToJson {

    val grades: Map<CurveClass, Grade> = grades.toMap()

    val grade: Grade get() = grades.maxBy { it.value }!!.value

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