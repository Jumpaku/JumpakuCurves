package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.*
import jumpaku.core.affine.transform.Rotate
import jumpaku.core.affine.transform.Transform
import jumpaku.core.affine.transform.Translate
import jumpaku.core.affine.transform.UniformlyScale
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
        val axis: Vector,
        val radian: Double = 0.0,
        val fuzziness: Double = 0.0,
        val resolution: Int = 0): ToJson {

    val isNoGrid: Boolean = 1.0.divOption(spacing).isEmpty

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    val localToWorld: Transform get() = Rotate(axis, radian)
            .andThen(UniformlyScale(spacing))
            .andThen(Translate(origin - Point.origin))

    fun snapToNearestGrid(cursor: Point): GridPoint = localToWorld.invert().get()(cursor)
            .toArray()
            .map { FastMath.round(it) }
            .let { (x, y, z) -> GridPoint(x, y, z) }

    fun deriveGrid(resolution: Int): Grid = Grid(spacing = spacing(spacing, magnification, resolution),
            magnification = magnification,
            origin = origin,
            axis = axis,
            radian = radian,
            fuzziness = gridFuzziness(fuzziness, magnification, resolution),
            resolution = resolution)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "spacing" to spacing.toJson(),
            "magnification" to magnification.toJson(),
            "origin" to origin.toJson(),
            "axis" to axis.toJson(),
            "radian" to radian.toJson(),
            "fuzziness" to fuzziness.toJson(),
            "resolution" to resolution.toJson())

    companion object {

        fun noGrid(baseGrid: Grid): Grid = Grid(
                spacing = 0.0,
                magnification = baseGrid.magnification,
                origin = baseGrid.origin,
                axis = baseGrid.axis,
                radian = baseGrid.radian,
                fuzziness = 0.0,
                resolution = Int.MAX_VALUE)

        fun spacing(baseGridSpacing: Double, magnification: Int, resolution: Int): Double =
                baseGridSpacing * FastMath.pow(magnification.toDouble(), -resolution)

        fun gridFuzziness(baseGridFuzziness: Double, magnification: Int, resolution: Int): Double =
                baseGridFuzziness * FastMath.pow(magnification.toDouble(), -resolution)

        fun fromJson(json: JsonElement): Option<Grid> = Try.ofSupplier { Grid(
                    spacing = json["spacing"].double,
                    magnification = json["magnification"].int,
                    origin = Point.fromJson(json["origin"]).get(),
                    axis = Vector.fromJson(json["axis"]).get(),
                    radian = json["radian"].double,
                    fuzziness = json["fuzziness"].double,
                    resolution = json["resolution"].int)
        }.toOption()
    }
}
