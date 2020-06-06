package jumpaku.curves.fsc.snap.point

import jumpaku.commons.control.Option
import jumpaku.commons.control.toOption
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint

class MFGS(val minResolution: Int = 0, val maxResolution: Int = 0) : PointSnapper {

    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    override fun snap(grid: Grid, cursor: Point): Option<PointSnapResult> {
        class TmpResult(val resolution: Int, val gridPoint: GridPoint, val necessity: Grade)

        val candidates = (minResolution..maxResolution).map {
            val gridPoint = grid.snapToNearestGrid(cursor, it)
            val grade = grid.transformToWorld(gridPoint, it).isNecessary(cursor)
            TmpResult(it, gridPoint, grade)
        }
        val necessities = candidates.map { it.necessity }
        val mus = necessities.mapIndexed { i, ni -> necessities.take(i).fold(ni) { n, nj -> n and !nj } }
        return candidates.zip(mus)
                .maxBy { (_, grade) -> grade }.toOption()
                .map { (result, mu) -> PointSnapResult(result.resolution, result.gridPoint, mu) }
                .filter { it.grade.toBoolean() }
    }
}