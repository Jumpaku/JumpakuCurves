package jumpaku.examples

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.parseJson
import java.nio.file.Paths

object BSplineConverter {

    fun toJsonOld(s: BSpline): JsonElement = jsonObject(
            "controlPoints" to jsonArray(s.controlPoints.map { it.toJson() }),
            "knotVector" to s.knotVector.toJson())

    fun fromJsonOld(j: JsonElement): BSpline =
            BSpline(
                    j["controlPoints"].array.map { Point.fromJson(it) }.dropLast(1),
                    KnotVector(
                            j["knotVector"]["degree"].int,
                            j["knotVector"]["knots"].array.toList().dropLast(1).groupBy { it.double }.map { (k, v) ->
                                Knot(k, v.size)
                            }))

    fun toJsonNew(s: BSpline): JsonElement = jsonObject(
            "controlPoints" to jsonArray(s.controlPoints.map { it.toJson() }),
            "degree" to s.degree,
            "knots" to jsonArray(s.knotVector.knots.map { it.toJson() }))

    fun fromJsonNew(j: JsonElement): BSpline {
        val d = j["degree"].int
        val cp = j["controlPoints"].array.map { Point.fromJson(it) }
        val ks = j["knots"].array.map { Knot.fromJson(it) }
        return BSpline(cp, KnotVector(d, ks))
    }
}

fun main() {
    println(Paths.get(".").toAbsolutePath())
    Paths.get("oldfsc").toFile().walkBottomUp().forEach {
        if (it.isFile && it.extension == "json") {
            print(it.path + ":")
            val s = it.parseJson().tryMap { BSplineConverter.fromJsonOld(it) }.orThrow()

            val newJson = BSplineConverter.toJsonNew(s)
            val newBSpline = BSplineConverter.fromJsonNew(newJson)
            val samecp = s.controlPoints.zip(newBSpline.controlPoints).all { (a, e) -> a == e }
            val sameknot = s.knotVector.knots.zip(newBSpline.knotVector.knots).all { (a, e) ->
                a.multiplicity == e.multiplicity && a.value == e.value
            }
            println(samecp && sameknot)

            Paths.get("newfsc", it.name).toFile().writeText(s.toString())
        }
    }
}