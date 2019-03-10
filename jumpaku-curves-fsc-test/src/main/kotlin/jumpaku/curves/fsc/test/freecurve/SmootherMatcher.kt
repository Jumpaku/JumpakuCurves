package jumpaku.curves.fsc.test.freecurve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.freecurve.Smoother
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Smoother, expected: Smoother, error: Double = 1.0e-9): Boolean =
        actual.samplingFactor == expected.samplingFactor &&
                isCloseTo(actual.pruningFactor, expected.pruningFactor, error)

fun closeTo(expected: Smoother, precision: Double = 1.0e-9): TypeSafeMatcher<Smoother> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
