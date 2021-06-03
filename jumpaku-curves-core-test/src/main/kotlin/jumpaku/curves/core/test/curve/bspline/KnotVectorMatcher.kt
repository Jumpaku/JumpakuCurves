package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.test.matcher
import jumpaku.commons.math.test.isCloseTo
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.core.curve.bspline.KnotVector
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: KnotVector, expected: KnotVector, error: Double = 1.0e-9): Boolean =
    (actual.size == expected.size) &&
            actual.zip(expected).all { (a, e) -> isCloseTo(a, e, error) } &&
            actual.degree == expected.degree &&
            isCloseTo(actual.domain, expected.domain, error)

fun closeTo(expected: KnotVector, precision: Double = 1.0e-9): TypeSafeMatcher<KnotVector> =
    matcher("close to <$expected> with precision $precision") { actual ->
        isCloseTo(actual, expected, precision)
    }

