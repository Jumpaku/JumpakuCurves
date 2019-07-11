package jumpaku.curves.experimental.fsc.edit


import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.control.toJson
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import java.util.*

sealed class Element : ToJson {

    data class Id(val elementId: String)

    data class Connector(val body: Point, val front: Option<Point>, val back: Option<Point>) : Element() {

        override fun toJson(): JsonElement = jsonObject(
                "elementClass" to "Connector".toJson(),
                "body" to body.toJson(),
                "front" to front.map { it.toJson() }.toJson(),
                "back" to back.map { it.toJson() }.toJson())
        override fun toString(): String = toJsonString()
    }

    class Identified(val fragment: BSpline) : Element() {

        val front: Point = fragment.evaluate(fragment.domain.begin)

        val back: Point = fragment.evaluate(fragment.domain.end)

        override fun toJson(): JsonElement = jsonObject(
                "elementClass" to "Identified".toJson(),
                "fragment" to fragment.toJson())
        override fun toString(): String = toJsonString()
    }

    fun withId(): Pair<Id, Element> = randomId() to this

    companion object {

        fun randomId(): Id = Id(UUID.randomUUID().toString())

        fun connector(body: Point, first: Option<Point>, last: Option<Point>): Pair<Id, Connector> =
                randomId() to Connector(body, first, last)

        fun identified(fragment: BSpline): Pair<Id, Identified> = randomId() to Identified(fragment)

        fun fromJson(json: JsonElement): Element = when(json["elementClass"].string) {
            "Connector" -> Connector(
                    Point.fromJson(json["body"]),
                    Option.fromJson(json["front"]).map { Point.fromJson(it) },
                    Option.fromJson(json["back"]).map { Point.fromJson(it) })
            "Identified" -> Identified(BSpline.fromJson(json["fragment"]))
            else -> error("invalid elementClass ${json["elementClass"].string}")
        }
    }
}
