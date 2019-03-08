package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.commons.test.matcher
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: WeightedPoint, expected: WeightedPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                jumpaku.commons.test.isCloseTo(actual.weight, expected.weight, error)

fun closeTo(expected: WeightedPoint, precision: Double = 1.0e-9): TypeSafeMatcher<WeightedPoint> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

