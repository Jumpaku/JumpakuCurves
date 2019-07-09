package jumpaku.curves.fsc.test.blend

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.test.generate.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: BlendGenerator, expected: BlendGenerator, error: Double = 1.0e-9): Boolean =
        actual.degree == expected.degree &&
        isCloseTo(actual.knotSpan, expected.knotSpan, error) &&
                isCloseTo(actual.bandWidth, expected.bandWidth, error) &&
                isCloseTo(actual.dataPreparer, expected.dataPreparer, error) &&
                isCloseTo(actual.fuzzifier, expected.fuzzifier, error)

fun closeTo(expected: BlendGenerator, precision: Double = 1.0e-9): TypeSafeMatcher<BlendGenerator> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
