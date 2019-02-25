package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Interval, expected: Interval, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                isCloseTo(actual.end, expected.end, error)

fun closeTo(expected: Interval, precision: Double = 1.0e-9): TypeSafeMatcher<Interval> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun Interval.shouldEqualToInterval(expected: Interval, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
