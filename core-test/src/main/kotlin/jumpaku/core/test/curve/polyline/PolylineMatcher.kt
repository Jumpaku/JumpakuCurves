package jumpaku.core.test.curve.polyline

import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Polyline, expected: Polyline, error: Double = 1.0e-9): Boolean =
        (actual.points.size == expected.points.size) &&
                actual.points.zip(expected.points) { a, e -> isCloseTo(a, e, error) }.all { it }


fun Polyline.shouldEqualToPolyline(expected: Polyline, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
