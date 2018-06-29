package jumpaku.fsc.snap.point

import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.geom.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.snap.Grid


class MFGS(
        val baseGrid: Grid,
        val minResolution: Int = 0,
        val maxResolution: Int = 0): PointSnapper {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    override fun snap(cursor: Point): Option<PointSnapResult> {
        val candidates = Stream.rangeClosed(minResolution, maxResolution).map {
            val grid = baseGrid.deriveGrid(it)
            val gridPoint = grid.snapToNearestGrid(cursor)
            val worldPoint = gridPoint.toWorldPoint(grid.localToWorld)
            PointSnapResult(grid, gridPoint, worldPoint, worldPoint.copy(r = grid.fuzziness).isNecessary(cursor))
        }.toArray()
        val mus = candidates
                .scanLeft(Stream.empty<PointSnapResult>()) { acc, n -> acc.append(n) }.tail()
                .map { ns -> ns.init().map { !it.grade }.fold(ns.last().grade, Grade::and) }
        return mus.zip(candidates)
                .find { (mu, _) -> mu >= Grade(0.5) }
                .map { (mu, result) -> result.copy(grade = mu) }
    }
}