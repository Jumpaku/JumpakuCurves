package jumpaku.curves.core.geom

import jumpaku.commons.control.Result
import jumpaku.commons.control.result


class Plane(val p0: Point, val p1: Point, val p2: Point) {

    init {
        require((p1 - p0).cross(p2 - p0).run { div(length()).isSuccess }) { "p0 and p1 are degenerated" }
    }
}

/**
 * Creates a line defined by p0, p1 and p2.
 * Fails when the triangle defined by p0, p1 and p2 is regarded as a degenerated line segment or point.
 */
fun plane(p0: Point, p1: Point, p2: Point): Result<Plane> = result { Plane(p0, p1, p2) }
