package jumpaku.curves.fsc.identify.nquarter

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.commons.json.jsonMap
import jumpaku.commons.json.map
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reference.ReferenceJson

class NQuarterIdentifyResult(grades: Map<NQuarterClass, Grade>,
                             val nQuarter1: Reference,
                             val nQuarter2: Reference,
                             val nQuarter3: Reference) {

    init {
        require(grades.isNotEmpty()) { "empty grades" }
    }

    val grades: Map<NQuarterClass, Grade> = grades.toMap()


    val nQuarterClass: NQuarterClass = grades.maxByOrNull { (_, grade) -> grade }!!.key

    val grade: Grade = grades[nQuarterClass]!!
}

object NQuarterIdentifyResultJson : JsonConverterBase<NQuarterIdentifyResult>() {

    override fun toJson(src: NQuarterIdentifyResult): JsonElement = src.run {
        jsonObject(
                "grades" to jsonMap(grades.map { (k, v) -> k.name.toJson() to GradeJson.toJson(v) }.toMap()),
                "nQuarter1" to ReferenceJson.toJson(nQuarter1),
                "nQuarter2" to ReferenceJson.toJson(nQuarter2),
                "nQuarter3" to ReferenceJson.toJson(nQuarter3))
    }

    override fun fromJson(json: JsonElement): NQuarterIdentifyResult = NQuarterIdentifyResult(
            json["grades"].map.map { (k, v) ->
                NQuarterClass.valueOf(k.string) to GradeJson.fromJson(v.asJsonPrimitive)
            }.toMap(),
            ReferenceJson.fromJson(json["nQuarter1"]),
            ReferenceJson.fromJson(json["nQuarter2"]),
            ReferenceJson.fromJson(json["nQuarter3"]))
}
