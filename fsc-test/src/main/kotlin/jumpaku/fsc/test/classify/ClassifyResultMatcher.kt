
package jumpaku.fsc.test.classify

import jumpaku.core.test.isCloseTo
import jumpaku.fsc.classify.ClassifyResult
import org.amshove.kluent.should


fun isCloseTo(actual: ClassifyResult, expected: ClassifyResult, error: Double = 1.0e-9): Boolean =
        actual.grades.size() == expected.grades.size() &&
                actual.grades.keySet().eq(expected.grades.keySet()) &&
                actual.grades.keySet().all {
                    isCloseTo(actual.grades[it].get().value, expected.grades[it].get().value, error)
                }

fun ClassifyResult.shouldBeClassifyResult(expected: ClassifyResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
