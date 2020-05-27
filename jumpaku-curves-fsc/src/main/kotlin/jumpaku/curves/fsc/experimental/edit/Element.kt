package jumpaku.curves.fsc.experimental.edit


import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import java.util.*

sealed class Element  {

    data class Connector(val body: Point, val front: Option<Point>, val back: Option<Point>) : Element() {
/*
        override fun toJson(): JsonElement = jsonObject(
                "elementClass" to "Connector".toJson(),
                "body" to body.toJson(),
                "front" to front.map { it.toJson() }.toJson(),
                "back" to back.map { it.toJson() }.toJson())
        override fun toString(): String = toJsonString()*/
    }

    class Target(val fragment: BSpline) : Element() {

        val front: Point = fragment.evaluate(fragment.domain.begin)

        val back: Point = fragment.evaluate(fragment.domain.end)
/*
        override fun toJson(): JsonElement = jsonObject(
                "elementClass" to "Target".toJson(),
                "fragment" to fragment.toJson())
        override fun toString(): String = toJsonString()*/
    }

    fun withId(): Pair<Id, Element> = randomId() to this

    companion object {

        fun randomId(): Id = Id(UUID.randomUUID().toString())

        fun connector(body: Point, first: Option<Point>, last: Option<Point>): Pair<Id, Connector> =
                randomId() to Connector(body, first, last)

        fun target(fragment: BSpline): Pair<Id, Target> = randomId() to Target(fragment)
/*
        fun fromJson(json: JsonElement): Element = when(json["elementClass"].string) {
            "Connector" -> Connector(
                    Point.fromJson(json["body"]),
                    Option.fromJson(json["front"]).map { Point.fromJson(it) },
                    Option.fromJson(json["back"]).map { Point.fromJson(it) })
            "Target" -> Target(BSpline.fromJson(json["fragment"]))
            else -> error("invalid elementClass ${json["elementClass"].string}")
        }*/
    }
}
