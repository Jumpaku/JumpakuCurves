package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.isCloseTo
import org.amshove.kluent.should


fun isCloseTo(actual: Point, expected: Point, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.toVector(), expected.toVector(), error) &&
                isCloseTo(actual.r, expected.r, error)

fun Point.shouldEqualToPoint(expected: Point, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
