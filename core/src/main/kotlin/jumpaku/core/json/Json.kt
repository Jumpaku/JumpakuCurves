package jumpaku.core.json

import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.vavr.control.Try
import jumpaku.core.affine.Point
import jumpaku.core.curve.bspline.BSpline
import io.vavr.collection.Array
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector

val prettyGson = GsonBuilder().setPrettyPrinting().create()!!

fun String.parseToJson(): Try<JsonElement> = Try.ofSupplier { JsonParser().parse(this) }


fun Point.toJson(): JsonElement = jsonObject("x" to x, "y" to y, "z" to z, "r" to r)
val JsonElement.point: Point get() = Point(this["x"].double, this["y"].double, this["z"].double, this["r"].double)

fun Interval.toJson(): JsonElement = jsonObject("begin" to begin, "end" to end)
val JsonElement.interval: Interval get() = Interval(this["begin"].double, this["end"].double)

fun KnotVector.toJson(): JsonElement = jsonObject("degree" to degree, "knots" to jsonArray(knots.map { it.toJson() }))
val JsonElement.knotVector: KnotVector get() = KnotVector(this["degree"].int, this["knots"].array.map { it.double })

fun BSpline.toJson(): JsonElement = jsonObject(
        "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
        "knotVector" to knotVector.toJson())
val JsonElement.bSpline: BSpline get() = BSpline(
        this["controlPoints"].array.map { it.point },
        this["knotVector"].knotVector)


fun main(vararg args: String) {
    val x = BSpline(
            Array.of(Point.x(1.0), Point.x(2.0), Point.x(3.0), Point.x(4.0)),
            KnotVector.Companion.clampedUniform(3, 8))
    println(x.toJson())
    println(x.toJson().toString().parseToJson().get().bSpline)
}
