package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Bezier, expected: Bezier, error: Double = 1.0e-9): Boolean =
        (actual.controlPoints.size == expected.controlPoints.size) &&
                actual.controlPoints.zip(expected.controlPoints) { a, e -> isCloseTo(a, e, error) }.all { it }

fun closeTo(expected: Bezier, precision: Double = 1.0e-9): TypeSafeMatcher<Bezier> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

