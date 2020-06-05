package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object FuzzifierJson : JsonConverterBase<Fuzzifier>() {

    override fun toJson(src: Fuzzifier): JsonElement = when (src) {
        is Fuzzifier.Linear -> src.run {
            jsonObject(
                    "type" to "Linear",
                    "velocityCoefficient" to velocityCoefficient,
                    "accelerationCoefficient" to accelerationCoefficient)
        }
    }

    override fun fromJson(json: JsonElement): Fuzzifier = when (json["type"].string) {
        "Linear" -> Fuzzifier.Linear(json["velocityCoefficient"].double, json["accelerationCoefficient"].double)
        else -> error("invalid fuzzifier type: ${json["type"].string}")
    }
}