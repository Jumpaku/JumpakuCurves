package jumpaku.curves.fsc.test.generate

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.generate.DataPreparer
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: DataPreparer, expected: DataPreparer, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.fillSpan, expected.fillSpan, error) &&
                isCloseTo(actual.extendInnerSpan, expected.extendInnerSpan, error) &&
                isCloseTo(actual.extendOuterSpan, expected.extendOuterSpan, error) &&
                actual.extendDegree == expected.extendDegree

fun closeTo(expected: DataPreparer, precision: Double = 1.0e-9): TypeSafeMatcher<DataPreparer> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
