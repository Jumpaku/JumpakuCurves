package jumpaku.fsc.test.identify.nquarter

import jumpaku.fsc.identify.nquarter.NQuarterIdentifyResult
import org.amshove.kluent.should

fun isCloseTo(a: NQuarterIdentifyResult, e: NQuarterIdentifyResult, error: Double = 1e-10): Boolean =
    jumpaku.core.test.isCloseTo(a.grade.value, e.grade.value, error) &&
            e.grades.keys.all { nQuarterClass ->
                jumpaku.core.test.isCloseTo(a.grades[nQuarterClass]!!.value, e.grades[nQuarterClass]!!.value, error)
            } &&
            a.nQuarterClass == e.nQuarterClass &&
            jumpaku.fsc.test.identify.reference.isCloseTo(a.nQuarter1, e.nQuarter1, error) &&
            jumpaku.fsc.test.identify.reference.isCloseTo(a.nQuarter2, e.nQuarter2, error) &&
            jumpaku.fsc.test.identify.reference.isCloseTo(a.nQuarter3, e.nQuarter3, error)

fun NQuarterIdentifyResult.shouldBeCloseTo(expected: NQuarterIdentifyResult, error: Double = 1.0e-9): NQuarterIdentifyResult = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}