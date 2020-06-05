package jumpaku.curves.core.curve

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson

object ParamPointJson : JsonConverterBase<ParamPoint>() {

    override fun toJson(src: ParamPoint): JsonElement = src.run {
        jsonObject("point" to PointJson.toJson(point), "param" to param.toJson())
    }

    override fun fromJson(json: JsonElement): ParamPoint = ParamPoint(PointJson.fromJson(json["point"]), json["param"].double)
}