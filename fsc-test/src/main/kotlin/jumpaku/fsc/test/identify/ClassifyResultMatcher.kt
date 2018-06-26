
package jumpaku.fsc.test.identify

import jumpaku.core.test.isCloseTo
import jumpaku.fsc.identify.IdentifyResult
import jumpaku.fsc.test.identify.reference.isCloseTo
import org.amshove.kluent.should


fun isCloseTo(actual: IdentifyResult, expected: IdentifyResult, error: Double = 1.0e-9): Boolean =
        actual.grades.size() == expected.grades.size()
                && actual.grades.keySet().eq(expected.grades.keySet())
                && actual.grades.keySet().all {
                    isCloseTo(actual.grades[it].get().value, expected.grades[it].get().value, error)
                }
                && isCloseTo(actual.linear, expected.linear, error)
                && isCloseTo(actual.circular, expected.circular, error)
                && isCloseTo(actual.elliptic, expected.elliptic, error)

fun IdentifyResult.shouldEqualToClassifyResult(expected: IdentifyResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
