package jumpaku.curves.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.PointJson
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.util.FastMath

open class Grid(
        val baseSpacing: Double,
        val baseFuzziness: Double = 0.0,
        val magnification: Int = 2,
        val origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        val rotation: Rotate = Rotate(Vector.K, 0.0)) {

    open fun spacing(resolution: Int): Double = baseSpacing * FastMath.pow(magnification.toDouble(), -resolution)

    open fun fuzziness(resolution: Int): Double = baseFuzziness * FastMath.pow(magnification.toDouble(), -resolution)

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    fun localToWorld(resolution: Int): Transform = rotation
            .andThen(UniformlyScale(spacing(resolution)))
            .andThen(Translate(origin - Point.origin))

    fun snapToNearestGrid(cursor: Point, resolution: Int): GridPoint = localToWorld(resolution).invert().orThrow()(cursor)
            .let { (x, y, z) -> GridPoint(FastMath.round(x), FastMath.round(y), FastMath.round(z)) }

    fun transformToWorld(gridPoint: GridPoint, resolution: Int): Point = gridPoint.run {
        localToWorld(resolution)(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))
                .copy(r = fuzziness(resolution))
    }
}

object GridJson : JsonConverterBase<Grid>() {

    override fun toJson(src: Grid): JsonElement = src.run {
        jsonObject(
                "baseSpacing" to baseSpacing.toJson(),
                "magnification" to magnification.toJson(),
                "origin" to PointJson.toJson(origin),
                "rotation" to RotateJson.toJson(rotation),
                "baseFuzziness" to baseFuzziness.toJson())
    }

    override fun fromJson(json: JsonElement): Grid = Grid(
            baseSpacing = json["baseSpacing"].double,
            magnification = json["magnification"].int,
            origin = PointJson.fromJson(json["origin"]),
            rotation = RotateJson.fromJson(json["rotation"]),
            baseFuzziness = json["baseFuzziness"].double)
}
