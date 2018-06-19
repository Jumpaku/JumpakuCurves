package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.*
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Transform
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.util.divOption
import org.apache.commons.math3.util.FastMath


class Grid(
        val spacing: Double,
        val magnification: Int = 2,
        val origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        val rotation: Rotate = Rotate(Vector.K, 0.0),
        val fuzziness: Double = 0.0,
        val resolution: Int = 0): ToJson {

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    val localToWorld: Transform get() = rotation
            .andThen(UniformlyScale(spacing))
            .andThen(Translate(origin - Point.origin))

    fun snapToNearestGrid(cursor: Point): GridPoint = localToWorld.invert().get()(cursor)
            .toArray()
            .map { FastMath.round(it) }
            .let { (x, y, z) -> GridPoint(x, y, z) }

    fun deriveGrid(resolution: Int): Grid = Grid(spacing = spacing(spacing, magnification, resolution),
            magnification = magnification,
            origin = origin,
            rotation = rotation,
            fuzziness = gridFuzziness(fuzziness, magnification, resolution),
            resolution = resolution)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "spacing" to spacing.toJson(),
            "magnification" to magnification.toJson(),
            "origin" to origin.toJson(),
            "rotation" to rotation.toJson(),
            "fuzziness" to fuzziness.toJson(),
            "resolution" to resolution.toJson())

    companion object {

        fun spacing(baseGridSpacing: Double, magnification: Int, resolution: Int): Double =
                baseGridSpacing * FastMath.pow(magnification.toDouble(), -resolution)

        fun gridFuzziness(baseGridFuzziness: Double, magnification: Int, resolution: Int): Double =
                baseGridFuzziness * FastMath.pow(magnification.toDouble(), -resolution)

        fun fromJson(json: JsonElement): Option<Grid> = Try.ofSupplier { Grid(
                spacing = json["spacing"].double,
                magnification = json["magnification"].int,
                origin = Point.fromJson(json["origin"]).get(),
                rotation = Rotate.fromJson(json["rotation"]).get(),
                fuzziness = json["fuzziness"].double,
                resolution = json["resolution"].int)
        }.toOption()
    }
}
