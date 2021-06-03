package jumpaku.curves.core.curve.bspline_old

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.curve.KnotJson
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.geom.WeightedPointJson

object NurbsJson : JsonConverterBase<Nurbs>() {

    override fun toJson(src: Nurbs): JsonElement = src.run {
        jsonObject(
                "weightedControlPoints" to jsonArray(weightedControlPoints.map { WeightedPointJson.toJson(it) }),
                "degree" to degree,
                "knots" to jsonArray(knotVector.knots.map { KnotJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): Nurbs {
        val d = json["degree"].int
        val wcp = json["weightedControlPoints"].array.map { WeightedPointJson.fromJson(it) }
        val ks = json["knots"].array.map { KnotJson.fromJson(it) }
        return Nurbs(wcp, KnotVector(d, ks))
    }

}