package jumpaku.fsc.snap.point

import io.vavr.collection.Stream
import jumpaku.core.geom.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.Option
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.toOption
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.toWorldPoint


class MFGS(
        val minResolution: Int = 0,
        val maxResolution: Int = 0): PointSnapper {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    override fun snap(grid: Grid, cursor: Point): Option<PointSnapResult> {
        val candidates = Stream.rangeClosed(minResolution, maxResolution).map {
            val gridPoint = grid.snapToNearestGrid(cursor, it)
            val grade = grid.toWorldPoint(gridPoint, it).copy(r = grid.fuzziness(it)).isNecessary(cursor)
            PointSnapResult(it, gridPoint, grade)
        }.toArray()
        val mus = candidates
                .scanLeft(Stream.empty<PointSnapResult>()) { acc, n -> acc.append(n) }.tail()
                .map { ns -> ns.init().map { !it.grade }.fold(ns.last().grade, Grade::and) }
        return mus.zip(candidates)
                .find { (mu, _) -> mu >= Grade(0.5) }.orNull.toOption()
                .map { (mu, result) -> result.copy(grade = mu) }
    }
}