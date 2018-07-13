package jumpaku.fsc.snap.point

import io.vavr.control.Option
import jumpaku.core.geom.Point
import jumpaku.fsc.snap.Grid

interface PointSnapper {
    fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>
}