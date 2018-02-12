package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.core.affine.*
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath


sealed class Grid(
        val spacing: Double,
        val magnification: Int,
        val origin: Point,
        val fuzziness: Double,
        val resolution: Int) {

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    val localToWorld: Affine get() = identity.andScale(spacing).andTranslateTo(origin)

    fun snapToNearestGrid(cursor: Point): GridPoint = localToWorld.invert()(cursor)
            .toArray()
            .map { FastMath.round(it) }
            .let { (x, y, z) -> GridPoint(x, y, z) }
}

class BaseGrid(
        spacing: Double,
        magnification: Int = 4,
        origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        fuzziness: Double = 0.0
): Grid(spacing = spacing,
        magnification = magnification,
        origin = origin,
        fuzziness = fuzziness,
        resolution = 0) {

    fun deriveGrid(resolution: Int): Grid = if (resolution == 0) this else DerivedGrid(this, resolution)
}

class DerivedGrid(
        val baseGrid: BaseGrid,
        resolution: Int
): Grid(spacing = spacing(baseGrid.spacing, baseGrid.magnification, resolution),
        magnification = baseGrid.magnification,
        origin = baseGrid.origin,
        fuzziness = gridFuzziness(baseGrid.fuzziness, baseGrid.magnification, resolution),
        resolution = resolution) {

    companion object {

        private fun spacing(baseGridSpacing: Double, baseGridMagnification: Int, resolution: Int): Double =
                baseGridSpacing * FastMath.pow(baseGridMagnification.toDouble(), -resolution)

        private fun gridFuzziness(baseGridFuzziness: Double, baseGridMagnification: Int, resolution: Int): Double =
                baseGridFuzziness * FastMath.pow(baseGridMagnification.toDouble(), -resolution)
    }
}

class NoGrid(
        val baseGrid: BaseGrid
): Grid(spacing = 0.0,
        magnification = baseGrid.magnification,
        origin = baseGrid.origin,
        fuzziness = 0.0,
        resolution = Int.MAX_VALUE)