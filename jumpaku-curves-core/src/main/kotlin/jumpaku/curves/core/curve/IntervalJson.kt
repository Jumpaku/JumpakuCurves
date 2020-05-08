package jumpaku.curves.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import jumpaku.commons.json.JsonConverterBase


object IntervalJson : JsonConverterBase<Interval>() {

    override fun toJson(src: Interval): JsonElement = src.run {
        jsonObject("begin" to begin.toJson(), "end" to end.toJson())
    }

    override fun fromJson(json: JsonElement): Interval = Interval(json["begin"].double, json["end"].double)
}