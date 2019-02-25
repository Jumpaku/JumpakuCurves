package jumpaku.curves.core.test

import org.amshove.kluent.should
import org.apache.commons.math3.util.Precision

fun isCloseTo(actual: Double, expected: Double, error: Double = 1.0e-9): Boolean = Precision.equals(actual, expected, error)

fun Double.shouldBeCloseTo(expected: Double, error: Double = 1.0e-9) = this.should("$this should be close to $expected with precision $error") {
    isCloseTo(this, expected, error)
}
