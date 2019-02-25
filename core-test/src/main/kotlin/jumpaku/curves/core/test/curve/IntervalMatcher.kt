package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Interval, expected: Interval, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                jumpaku.curves.core.test.isCloseTo(actual.end, expected.end, error)

fun Interval.shouldEqualToInterval(expected: Interval, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
