package jumpaku.curves.core.test.curve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.curve.WeightedParamPoint
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: WeightedParamPoint, expected: WeightedParamPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.paramPoint, expected.paramPoint, error) &&
                isCloseTo(actual.weight, expected.weight, error)

fun closeTo(expected: WeightedParamPoint, precision: Double = 1.0e-9): TypeSafeMatcher<WeightedParamPoint> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
