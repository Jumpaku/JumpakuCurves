package jumpaku.curves.fsc.test.generate

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.fsc.generate.Generator
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Generator, expected: Generator, error: Double = 1.0e-9): Boolean =
        actual.degree == expected.degree &&
                isCloseTo(actual.knotSpan, expected.knotSpan, error) &&
                isCloseTo(actual.fillSpan, expected.fillSpan, error) &&
                isCloseTo(actual.extendInnerSpan, expected.extendInnerSpan, error) &&
                isCloseTo(actual.extendOuterSpan, expected.extendOuterSpan, error) &&
                actual.extendDegree == expected.extendDegree &&
                isCloseTo(actual.fuzzifier, expected.fuzzifier, error)

fun closeTo(expected: Generator, precision: Double = 1.0e-9): TypeSafeMatcher<Generator> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
