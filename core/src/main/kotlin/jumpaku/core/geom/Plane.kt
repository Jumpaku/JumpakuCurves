package jumpaku.core.geom

import jumpaku.core.util.Result
import jumpaku.core.util.result
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

private typealias CommonsMathPlane = org.apache.commons.math3.geometry.euclidean.threed.Plane

class Plane(val p0: Point, val p1: Point, val p2: Point) {

    init {
        require((p1 - p0).cross(p2 - p0).run { div(length()).isSuccess }) { "p0 and p1 are degenerated" }
    }

    internal val plane: CommonsMathPlane = CommonsMathPlane(
            Vector3D(p0.x, p0.y, p0.z), Vector3D(p1.x, p1.y, p1.z), Vector3D(p2.x, p2.y, p2.z),0.0)
}

/**
 * Creates a line defined by p0, p1 and p2.
 * Fails when the triangle defined by p0, p1 and p2 is regarded as a degenerated line segment or point.
 */
fun plane(p0: Point, p1: Point, p2: Point): Result<Plane> = result { Plane(p0, p1, p2) }
