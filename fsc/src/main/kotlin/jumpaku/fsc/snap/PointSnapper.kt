package jumpaku.fsc.snap

import io.vavr.Tuple2
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import org.apache.commons.math3.util.FastMath

data class PointSnapResult(
        val grid: Option<Grid>,
        val snappedGridCoordinate: Option<GridCoordinate>,
        val snappedPoint: Point)

class PointSnapper(
        val baseGrid: BaseGrid,
        val minResolution: Int = 0,
        val maxResolution: Int = 0) {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    fun snap(cursor: Point): PointSnapResult {
        val candidates = Stream.rangeClosed(minResolution, maxResolution)
                .map { baseGrid.deriveGrid(it).snap(cursor) }
                .reverse()
        val ns = candidates.map { c -> !c.toFuzzyPoint().isNecessary(cursor) }
        val mus = candidates.zipWithIndex { candidate, index ->
            val mu = ns.take(index).reduce(Grade::and) and ns[index]
            Tuple2(mu, candidate)
        }
        val snapped = mus.filter { (mu, _) -> mu >= Grade(0.5) } .map { (_, c) -> c }

        return PointSnapResult(
                snapped.map { it.grid }.toOption(),
                snapped.toOption(),
                snapped.map { it.toCrispPoint() }.getOrElse { cursor })
    }

    fun Grid.snap(point: Point): GridCoordinate {
        return affine(point).toArray().map { FastMath.round(it) }
                .let { (x, y, z) -> GridCoordinate(x, y, z, this) }
    }
}