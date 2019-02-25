
package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.fsc.identify.primitive.IdentifyResult
import jumpaku.curves.fsc.test.identify.primitive.reference.isCloseTo
import org.amshove.kluent.should


fun isCloseTo(actual: IdentifyResult, expected: IdentifyResult, error: Double = 1.0e-9): Boolean =
        actual.grades.size == expected.grades.size
                && actual.grades.asVavr().keySet().eq(expected.grades.asVavr().keySet())
                && actual.grades.keys.all {
                    isCloseTo(actual.grades[it]!!.value, expected.grades[it]!!.value, error)
                }
                && isCloseTo(actual.linear, expected.linear, error)
                && isCloseTo(actual.circular, expected.circular, error)
                && isCloseTo(actual.elliptic, expected.elliptic, error)

fun IdentifyResult.shouldEqualToClassifyResult(expected: IdentifyResult, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
