package jumpaku.curves.fsc.test.generate

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.fsc.generate.Fuzzifier
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Fuzzifier, expected: Fuzzifier, error: Double = 1.0e-9): Boolean = when {
    actual is Fuzzifier.Linear && expected is Fuzzifier.Linear ->
        isCloseTo(actual.velocityCoefficient, expected.velocityCoefficient, error) &&
                isCloseTo(actual.accelerationCoefficient, expected.accelerationCoefficient, error)
    else -> false
}

fun closeTo(expected: Fuzzifier, precision: Double = 1.0e-9): TypeSafeMatcher<Fuzzifier> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }
