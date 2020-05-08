package jumpaku.curves.fsc.test.merge

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.fsc.merge.Merger
import jumpaku.curves.fsc.test.generate.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Merger, expected: Merger, error: Double = 1.0e-9): Boolean =
        actual.degree == expected.degree &&
                isCloseTo(actual.knotSpan, expected.knotSpan, error) &&
                actual.extendDegree == expected.extendDegree &&
                isCloseTo(actual.extendInnerSpan, expected.extendInnerSpan, error) &&
                isCloseTo(actual.extendOuterSpan, expected.extendOuterSpan, error) &&
                isCloseTo(actual.overlapThreshold.value, expected.overlapThreshold.value, error) &&
                isCloseTo(actual.samplingSpan, expected.samplingSpan, error) &&
                isCloseTo(actual.mergeRate, expected.mergeRate, error) &&
                isCloseTo(actual.bandWidth, expected.bandWidth, error) &&
                isCloseTo(actual.fuzzifier, expected.fuzzifier, error)

fun closeTo(expected: Merger, precision: Double = 1.0e-9): TypeSafeMatcher<Merger> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
