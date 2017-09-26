package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.core.affine.*
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath


sealed class Grid: ToJson {

    abstract val baseGridSpacing: Double

    abstract val magnification: Int

    abstract val origin: Point

    abstract val rotation: Affine

    abstract val fuzziness: Double

    abstract val resolution: Int

    val gridSpacing: Double get() = baseGridSpacing * FastMath.pow(magnification.toDouble(), -resolution)

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  rotated by specified rotation,
     *  scaled by gridSpacing,
     *  translated to specified origin.
     */
    val localToWorld: Affine get() = rotation.andScale(gridSpacing).andTranslateTo(origin)

    /**
     * worldToLocal is the inverse of localToWorld
     */
    val worldToLocal: Affine get() = localToWorld.invert()

    fun snap(cursor: Point): GridCoordinate {
        return worldToLocal(cursor).toArray().map { FastMath.round(it) }
                .let { (x, y, z) -> GridCoordinate(x, y, z, this) }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "baseGridSpacing" to baseGridSpacing.toJson(),
            "magnification" to magnification.toJson(),
            "origin" to origin.toJson(),
            "rotation" to rotation.toJson(),
            "fuzziness" to fuzziness.toJson(),
            "resolution" to resolution.toJson())
}

val JsonElement.grid: Grid get() {
    val base = BaseGrid(this["baseGridSpacing"].double,
            this["magnification"].int,
            this["origin"].point,
            this["rotation"].affine,
            this["fuzziness"].double)
    val resolution = this["resolution"].int

    return if (resolution == 0) base else base.deriveGrid(resolution)
}

class BaseGrid(
        override val baseGridSpacing: Double,
        override val magnification: Int = 4,
        override val origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        override val rotation: Affine = identity,
        override val fuzziness: Double = 0.0): Grid() {

    override val resolution: Int = 0

    fun deriveGrid(resolution: Int): Grid = if (resolution == 0) this else DerivedGrid(this, resolution)
}

class DerivedGrid(baseGrid: BaseGrid, override val resolution: Int): Grid() {

    override val baseGridSpacing = baseGrid.baseGridSpacing

    override val magnification = baseGrid.magnification

    override val origin = baseGrid.origin

    override val rotation = baseGrid.rotation

    override val fuzziness: Double = baseGrid.fuzziness * FastMath.pow(magnification.toDouble(), -resolution)
}