package jumpaku.fsc.identify.nquarter

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.jsonMap
import jumpaku.core.json.map
import jumpaku.fsc.identify.primitive.reference.Reference

class NQuarterIdentifyResult(grades: Map<NQuarterClass, Grade>,
                             val nQuarter1: Reference,
                             val nQuarter2: Reference,
                             val nQuarter3: Reference): ToJson {

    init {
        require(grades.isNotEmpty()) { "empty grades" }
    }

    val grades: Map<NQuarterClass, Grade> = grades.toMap()

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grades" to jsonMap(grades.map { (k, v) -> k.name.toJson() to v.toJson() }.toMap()),
            "nQuarter1" to nQuarter1.toJson(),
            "nQuarter2" to nQuarter2.toJson(),
            "nQuarter3" to nQuarter3.toJson()
    )

    val nQuarterClass: NQuarterClass = grades.maxBy { (_, grade) -> grade }!!.key

    val grade: Grade = grades[nQuarterClass]!!

    companion object {

        fun fromJson(json: JsonElement): NQuarterIdentifyResult =
                NQuarterIdentifyResult(
                        json["grades"].map.map { (k, v) ->
                            NQuarterClass.valueOf(k.string) to Grade.fromJson(v.asJsonPrimitive)
                        }.toMap(),
                        Reference.fromJson(json["nQuarter1"]),
                        Reference.fromJson(json["nQuarter2"]),
                        Reference.fromJson(json["nQuarter3"])
                )
    }
}
