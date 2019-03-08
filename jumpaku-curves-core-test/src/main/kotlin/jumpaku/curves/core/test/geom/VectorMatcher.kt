package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.matcher
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Vector, expected: Vector, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.x, expected.x, error) &&
                isCloseTo(actual.y, expected.y, error) &&
                isCloseTo(actual.z, expected.z, error)

fun closeTo(expected: Vector, precision: Double = 1.0e-9): TypeSafeMatcher<Vector> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

