package jumpaku.fsc.test.fragment

import jumpaku.core.test.curve.isCloseTo
import jumpaku.fsc.fragment.Fragment
import org.amshove.kluent.should

fun isCloseTo(actual: Fragment, expected: Fragment, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.interval, expected.interval, error) && actual.type == expected.type

fun Fragment.shouldEqualToFragment(expected: Fragment, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}