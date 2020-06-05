package jumpaku.curves.fsc.snap.point

import jumpaku.commons.control.Option
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid

interface PointSnapper {

    fun snap(grid: Grid, cursor: Point): Option<PointSnapResult>
}

