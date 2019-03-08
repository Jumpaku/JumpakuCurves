package jumpaku.curves.fsc.test.fragment

import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.fragment.Fragment
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Fragment, expected: Fragment, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.interval, expected.interval, error) && actual.type == expected.type

fun closeTo(expected: Fragment, precision: Double = 1.0e-9): TypeSafeMatcher<Fragment> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

