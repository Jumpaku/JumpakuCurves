
package jumpaku.fsc.test.classify

import jumpaku.core.test.curve.rationalbezier.isCloseTo
import jumpaku.core.test.isCloseTo
import jumpaku.fsc.classify.ClassifyResult
import org.amshove.kluent.should


fun isCloseTo(actual: ClassifyResult, expected: ClassifyResult, error: Double = 1.0e-9): Boolean =
        actual.grades.size() == expected.grades.size()
                && actual.grades.keySet().eq(expected.grades.keySet())
                && actual.grades.keySet().all {
                    isCloseTo(actual.grades[it].get().value, expected.grades[it].get().value, error)
                }
                && isCloseTo(actual.references.linear, expected.references.linear, error)
                && isCloseTo(actual.references.circular, expected.references.circular, error)
                && isCloseTo(actual.references.elliptic, expected.references.elliptic, error)

fun ClassifyResult.shouldEqualToClassifyResult(expected: ClassifyResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
