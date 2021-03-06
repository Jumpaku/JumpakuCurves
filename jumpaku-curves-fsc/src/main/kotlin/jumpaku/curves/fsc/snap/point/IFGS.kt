package jumpaku.curves.fsc.snap.point

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.some
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid


object IFGS : PointSnapper {

    override fun snap(grid: Grid, cursor: Point): Option<PointSnapResult> {
        1.0.tryDiv(cursor.r).onFailure { return None }
        val h = generateSequence(0) { it + 1 }.first {
            val gi = grid.snapToNearestGrid(cursor, it)
            val ni = grid.transformToWorld(gi, it).isNecessary(cursor)
            ni >= Grade(0.5)
        }
        val k = generateSequence(h) { it - 1 }.first {
            val gi = grid.snapToNearestGrid(cursor, it - 1)
            val ni = grid.transformToWorld(gi, it - 1).isNecessary(cursor)
            ni < Grade(0.5)
        }
        val gk = grid.snapToNearestGrid(cursor, k)
        val nk = grid.transformToWorld(gk, k).isNecessary(cursor)

        return some(PointSnapResult(k, gk, nk))
    }
}