
package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.identify.primitive.IdentifyResult
import jumpaku.curves.fsc.test.identify.primitive.reference.isCloseTo
import org.hamcrest.TypeSafeMatcher


fun isCloseTo(actual: IdentifyResult, expected: IdentifyResult, error: Double = 1.0e-9): Boolean =
        actual.grades.size == expected.grades.size
                && actual.grades.keys == expected.grades.keys
                && actual.grades.keys.all {
                    isCloseTo(actual.grades[it]!!.value, expected.grades[it]!!.value, error)
                }
                && isCloseTo(actual.linear, expected.linear, error)
                && isCloseTo(actual.circular, expected.circular, error)
                && isCloseTo(actual.elliptic, expected.elliptic, error)

fun closeTo(expected: IdentifyResult, precision: Double = 1.0e-9): TypeSafeMatcher<IdentifyResult> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

