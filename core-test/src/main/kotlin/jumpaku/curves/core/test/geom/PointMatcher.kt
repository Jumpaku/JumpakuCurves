package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: Point, expected: Point, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.toVector(), expected.toVector(), error) &&
                isCloseTo(actual.r, expected.r, error)

fun closeTo(expected: Point, precision: Double = 1.0e-9): TypeSafeMatcher<Point> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun Point.shouldEqualToPoint(expected: Point, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
