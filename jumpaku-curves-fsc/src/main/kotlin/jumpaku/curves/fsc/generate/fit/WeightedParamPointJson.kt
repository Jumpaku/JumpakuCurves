package jumpaku.curves.fsc.generate.fit

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.ParamPointJson

object WeightedParamPointJson : JsonConverterBase<WeightedParamPoint>() {
    override fun toJson(src: WeightedParamPoint): JsonElement = src.run {
        jsonObject("paramPoint" to ParamPointJson.toJson(paramPoint), "weight" to weight.toJson())

    }

    override fun fromJson(json: JsonElement): WeightedParamPoint =
            WeightedParamPoint(ParamPointJson.fromJson(json["paramPoint"]), json["weight"].double)
}