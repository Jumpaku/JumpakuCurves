package jumpaku.fsc.snap.point

import jumpaku.core.geom.Point
import jumpaku.core.util.Option
import jumpaku.fsc.snap.Grid

interface PointSnapper {
    fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>
}