package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.fsc.test.identify.primitive.reference.isCloseTo
import jumpaku.commons.test.matcher
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifyResult
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(a: NQuarterIdentifyResult, e: NQuarterIdentifyResult, error: Double = 1e-10): Boolean =
        isCloseTo(a.grade.value, e.grade.value, error) &&
                e.grades.keys.all { nQuarterClass ->
                    isCloseTo(a.grades[nQuarterClass]!!.value, e.grades[nQuarterClass]!!.value, error)
                } &&
                a.nQuarterClass == e.nQuarterClass &&
                isCloseTo(a.nQuarter1, e.nQuarter1, error) &&
                isCloseTo(a.nQuarter2, e.nQuarter2, error) &&
                isCloseTo(a.nQuarter3, e.nQuarter3, error)

fun closeTo(expected: NQuarterIdentifyResult, precision: Double = 1.0e-9): TypeSafeMatcher<NQuarterIdentifyResult> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

