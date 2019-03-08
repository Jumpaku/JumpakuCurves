package jumpaku.curves.core.test.curve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Knot, expected: Knot, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.value, expected.value, error) && actual.multiplicity == expected.multiplicity

fun isCloseTo(actual: KnotVector, expected: KnotVector, error: Double = 1.0e-9): Boolean =
        (actual.knots.size == expected.knots.size) &&
                actual.knots.zip(expected.knots).all { (a, e) -> isCloseTo(a, e, error) } &&
                actual.degree == expected.degree &&
                isCloseTo(actual.domain, expected.domain, error)

fun closeTo(expected: KnotVector, precision: Double = 1.0e-9): TypeSafeMatcher<KnotVector> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

