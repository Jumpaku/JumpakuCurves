package jumpaku.curves.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.PointJson

object BSplineJson : JsonConverterBase<BSpline>() {

    override fun toJson(src: BSpline): JsonElement = src.run {
        jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { PointJson.toJson(it) }),
            "degree" to degree,
            "knots" to jsonArray(knotVector)
        )
    }

    override fun fromJson(json: JsonElement): BSpline {
        val d = json["degree"].int
        val cp = json["controlPoints"].array.map { PointJson.fromJson(it) }
        val ks = json["knots"].array.map { it.double }
        return BSpline(cp, KnotVector(d, ks))
    }
}