package jumpaku.fsc.snap.point

import io.vavr.collection.Stream
import jumpaku.core.geom.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.*
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.toWorldPoint


class MFGS(
        val minResolution: Int = 0,
        val maxResolution: Int = 0): PointSnapper {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    override fun snap(grid: Grid, cursor: Point): Option<PointSnapResult> {
        class TmpResult(val resolution: Int, val gridPoint: GridPoint, val necessity: Grade)
        val candidates = (minResolution..maxResolution).map {
            val gridPoint = grid.snapToNearestGrid(cursor, it)
            val grade = grid.toWorldPoint(gridPoint, it).copy(r = grid.fuzziness(it)).isNecessary(cursor)
            TmpResult(it, gridPoint, grade)
        }
        val mus = candidates.asVavr()
                .scanLeft(Stream.empty<TmpResult>()) { acc, n -> acc.append(n) }.tail()
                .map { ns -> ns.init().map { !it.necessity }.fold(ns.last().necessity, Grade::and) }
        return mus.zip(candidates)
                .find { (mu, _) -> mu >= Grade(0.5) }
                .toJOpt()
                .map { (mu, result) ->
                    result.run { PointSnapResult(resolution, gridPoint, mu) }
                }
    }
}