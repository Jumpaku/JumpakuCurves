package jumpaku.curves.core.test

import jumpaku.commons.math.test.isCloseTo
import jumpaku.commons.test.matcher
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: List<Double>, expected: List<Double>, error: Double = 1.0e-9): Boolean =
    (actual.size == expected.size) &&
            actual.zip(expected).all { (a, e) -> isCloseTo(a, e, error) }

fun closeTo(expected: List<Double>, precision: Double = 1.0e-9): TypeSafeMatcher<List<Double>> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }