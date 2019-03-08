package jumpaku.curves.fsc.fragment

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval

data class Fragment(val interval: Interval, val type: Type): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("interval" to interval.toJson(), "type" to type.name.toJson())

    enum class Type {
        Move, Stay
    }

    companion object {

        fun fromJson(json: JsonElement): Fragment =
            Fragment(Interval.fromJson(json["interval"]), Fragment.Type.valueOf(json["type"].string))
    }
}
