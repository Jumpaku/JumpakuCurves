package jumpaku.curves.fsc.identify.primitive

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.commons.json.jsonMap
import jumpaku.commons.json.map
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.fsc.identify.primitive.reference.ReferenceJson

object IdentifyResultJson : JsonConverterBase<IdentifyResult>() {

    override fun toJson(src: IdentifyResult): JsonElement = src.run {
        jsonObject(
                "grades" to jsonMap(grades.map { (k, v) -> k.name.toJson() to GradeJson.toJson(v) }.toMap()),
                "linear" to ReferenceJson.toJson(linear),
                "circular" to ReferenceJson.toJson(circular),
                "elliptic" to ReferenceJson.toJson(elliptic))
    }

    override fun fromJson(json: JsonElement): IdentifyResult = IdentifyResult(
            json["grades"].map.map { (c, g) -> CurveClass.valueOf(c.string) to GradeJson.fromJson(g.asJsonPrimitive) }.toMap(),
            ReferenceJson.fromJson(json["linear"]),
            ReferenceJson.fromJson(json["circular"]),
            ReferenceJson.fromJson(json["elliptic"]))
}