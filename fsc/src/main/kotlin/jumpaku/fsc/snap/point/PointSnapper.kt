package jumpaku.fsc.snap.point

import io.vavr.control.Option
import jumpaku.core.geom.Point

interface PointSnapper {
    fun snap(cursor: Point): Option<PointSnapResult>
}