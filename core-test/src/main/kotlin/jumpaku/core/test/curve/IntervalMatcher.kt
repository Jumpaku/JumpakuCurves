package jumpaku.core.test.curve

import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Interval, expected: Interval, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                jumpaku.core.test.isCloseTo(actual.end, expected.end, error)

fun Interval.shouldBeInterval(expected: Interval, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
