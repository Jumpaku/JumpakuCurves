package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.curve.Interval
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result

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
