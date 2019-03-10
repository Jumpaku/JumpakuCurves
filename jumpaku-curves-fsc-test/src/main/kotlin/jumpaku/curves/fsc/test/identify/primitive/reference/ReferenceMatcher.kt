package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.commons.test.matcher
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.core.test.curve.rationalbezier.isCloseTo
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Reference, expected: Reference, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.base, expected.base, error)
                && isCloseTo(actual.domain, expected.domain, error)

fun closeTo(expected: Reference, precision: Double = 1.0e-9): TypeSafeMatcher<Reference> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

