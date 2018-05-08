package jumpaku.fsc.test.fragment

import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.fragment.FragmentResult
import org.amshove.kluent.should


fun isCloseTo(actual: FragmentResult, expected: FragmentResult, error: Double = 1.0e-9): Boolean =
        actual.fragments.size() == expected.fragments.size() &&
                actual.fragments.zip(expected.fragments).all { (a, e) -> isCloseTo(a, e, error) }

fun FragmentResult.shouldBeFragmentResult(expected: FragmentResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
