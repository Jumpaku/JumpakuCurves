package jumpaku.examples

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.core.curve.Knot
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.Point
import jumpaku.core.json.parseJson
import java.nio.file.Paths

object BSplineConverter {

    fun toJsonOld(s: BSpline): JsonElement = jsonObject(
            "controlPoints" to jsonArray(s.controlPoints.map { it.toJson() }),
            "knotVector" to s.knotVector.toJson())

    fun fromJsonOld(j: JsonElement): BSpline =
            BSpline(j["controlPoints"].array.flatMap { Point.fromJson(it).value() },
                    KnotVector.fromJson(j["knotVector"]).orThrow())

    fun toJsonNew(s: BSpline): JsonElement = jsonObject(
            "controlPoints" to jsonArray(s.controlPoints.map { it.toJson() }),
            "degree" to s.degree,
            "knots" to jsonArray(s.knotVector.knots.map { it.toJson() }))

    fun fromJsonNew(j: JsonElement): BSpline {
        val d = j["degree"].int
        val cp = j["controlPoints"].array.flatMap { Point.fromJson(it).value() }
        val ks = j["knots"].array.flatMap { Knot.fromJson(it).value() }
        return BSpline(cp, KnotVector(d, ks))
    }
}

fun main(args: Array<String>) {
    Paths.get("fsctarget").toFile().walkBottomUp().forEach {
        if (it.extension == "json") {
            print(it.path + ":")
            val s = it.parseJson().tryMap { BSplineConverter.fromJsonOld(it) }.orThrow()

            val newJson = BSplineConverter.toJsonNew(s)
            val newBSpline = BSplineConverter.fromJsonNew(newJson)
            val samecp = s.controlPoints.zip(newBSpline.controlPoints).all { (a, e) -> a == e }
            val sameknot = s.knotVector.knots.zip(newBSpline.knotVector.knots).all { (a, e) ->
                a.multiplicity == e.multiplicity && a.value == e.value
            }
            println(samecp && sameknot)

            it.writeText(newJson.toString())
        }
    }
}