package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.curve.Interval
import jumpaku.core.curve.interval

data class Fragment(val interval: Interval, val type: Type) {

    override fun toString(): String = toJson().toString()

    fun toJson(): JsonElement = jsonObject("interval" to interval.toJson(), "type" to type.name.toJson())

    enum class Type {
        IDENTIFICATION,
        PARTITION
    }
}

val JsonElement.fragment: Fragment get() = Fragment(
        this["interval"].interval, Fragment.Type.valueOf(this["type"].string))
