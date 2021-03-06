package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: ConicSection, expected: ConicSection, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.begin, expected.begin, error) &&
                isCloseTo(actual.far, expected.far, error) &&
                isCloseTo(actual.end, expected.end, error) &&
                isCloseTo(actual.weight, expected.weight, error)

fun closeTo(expected: ConicSection, precision: Double = 1.0e-9): TypeSafeMatcher<ConicSection> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

