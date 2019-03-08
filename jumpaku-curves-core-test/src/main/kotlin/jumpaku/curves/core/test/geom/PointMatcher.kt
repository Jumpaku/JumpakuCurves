package jumpaku.curves.core.test.geom

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.geom.Point
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: Point, expected: Point, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.toVector(), expected.toVector(), error) &&
                isCloseTo(actual.r, expected.r, error)

fun closeTo(expected: Point, precision: Double = 1.0e-9): TypeSafeMatcher<Point> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

