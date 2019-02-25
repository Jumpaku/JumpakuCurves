package jumpaku.curves.fsc.snap.point

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.util.Option
import jumpaku.curves.fsc.snap.Grid

interface PointSnapper {
    fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>
}