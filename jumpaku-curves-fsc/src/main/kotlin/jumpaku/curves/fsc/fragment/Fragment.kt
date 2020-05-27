package jumpaku.curves.fsc.fragment

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.IntervalJson

data class Fragment(val interval: Interval, val type: Type) {

    override fun toString(): String = "Fragment(interval=$interval,type=$type)"

    enum class Type {
        Move, Stay
    }
}

object FragmentJson : JsonConverterBase<Fragment>() {

    override fun toJson(src: Fragment): JsonElement = src.run {
        jsonObject("interval" to IntervalJson.toJson(interval),
                "type" to type.name.toJson())
    }

    override fun fromJson(json: JsonElement): Fragment = Fragment(
            IntervalJson.fromJson(json["interval"]),
            Fragment.Type.valueOf(json["type"].string))

}