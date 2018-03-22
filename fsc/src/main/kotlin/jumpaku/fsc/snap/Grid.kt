package jumpaku.fsc.snap

import jumpaku.core.affine.*
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath


sealed class Grid(
        val spacing: Double,
        val magnification: Int,
        val origin: Point,
        val axis: Vector,
        val radian: Double,
        val fuzziness: Double,
        val resolution: Int) {

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    val localToWorld: Affine get() = identity.andRotate(axis, radian).andScale(spacing).andTranslate(origin - Point.origin)

    fun snapToNearestGrid(cursor: Point): GridPoint = localToWorld.invert().get()(cursor)
            .toArray()
            .map { FastMath.round(it) }
            .let { (x, y, z) -> GridPoint(x, y, z) }
}

class BaseGrid(
        spacing: Double,
        magnification: Int = 2,
        origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        axis: Vector = Vector.K,
        radian: Double = 0.0,
        fuzziness: Double = 0.0
): Grid(spacing = spacing,
        magnification = magnification,
        origin = origin,
        axis = axis,
        radian = radian,
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
        axis = baseGrid.axis,
        radian = baseGrid.radian,
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
        axis = baseGrid.axis,
        radian = baseGrid.radian,
        fuzziness = 0.0,
        resolution = Int.MAX_VALUE)