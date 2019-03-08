package jumpaku.curves.core.test.curve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.curve.Interval
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Interval, expected: Interval, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                isCloseTo(actual.end, expected.end, error)

fun closeTo(expected: Interval, precision: Double = 1.0e-9): TypeSafeMatcher<Interval> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

