package jumpaku.curves.fsc.test.fragment

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.fragment.Fragmenter
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Fragmenter, expected: Fragmenter, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.threshold.necessity.value, expected.threshold.necessity.value, error) &&
                isCloseTo(actual.threshold.possibility.value, expected.threshold.possibility.value, error) &&
                actual.chunkSize == expected.chunkSize &&
                isCloseTo(actual.minStayTimeSpan, actual.minStayTimeSpan, error)

fun closeTo(expected: Fragmenter, precision: Double = 1.0e-9): TypeSafeMatcher<Fragmenter> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
