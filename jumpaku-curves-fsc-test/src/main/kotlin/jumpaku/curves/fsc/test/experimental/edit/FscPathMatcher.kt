package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.experimental.edit.FscPath
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: FscPath, expected: FscPath, error: Double = 1.0e-9): Boolean =
        actual.keys == expected.keys && actual.keys.all { key ->
            isCloseTo(actual[key]!!, expected[key]!!, error)
        }

fun closeTo(expected: FscPath, precision: Double = 1.0e-9): TypeSafeMatcher<FscPath> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

