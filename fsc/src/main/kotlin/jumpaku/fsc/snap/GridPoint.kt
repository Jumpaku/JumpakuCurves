package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.Point
import jumpaku.core.transform.Transform
import jumpaku.core.json.ToJson

data class GridPoint(val x: Long, val y: Long, val z: Long): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "x" to x.toJson(),
            "y" to y.toJson(),
            "z" to z.toJson())

    fun toWorldPoint(localToWorld: Transform): Point = localToWorld(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))

    companion object {

        fun fromJson(json: JsonElement): Option<GridPoint> =
                Try.ofSupplier { GridPoint(json["x"].long, json["y"].long, json["z"].long) }.toOption()
    }
}
