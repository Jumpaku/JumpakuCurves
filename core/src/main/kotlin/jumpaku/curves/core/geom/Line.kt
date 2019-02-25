package jumpaku.curves.core.geom

import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result


class Line(val p0: Point, val p1: Point) {

    init {
        require((p1 - p0).run { div(length()).isSuccess }) { "p0 and p1 are degenerated" }
    }
}

/**
 * Creates a line defined by p0 and p1.
 * Fails when the line segment defined by p0 and p1 is regarded as a degenerated point.
 */
fun line(p0: Point, p1: Point): Result<Line> = result { Line(p0, p1) }