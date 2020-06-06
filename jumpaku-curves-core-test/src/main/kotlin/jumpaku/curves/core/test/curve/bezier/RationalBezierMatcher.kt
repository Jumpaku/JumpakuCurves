package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: RationalBezier, expected: RationalBezier, error: Double = 1.0e-9): Boolean =
        (actual.weightedControlPoints.size == expected.weightedControlPoints.size) &&
                actual.weightedControlPoints.zip(expected.weightedControlPoints) { a, e ->
                    isCloseTo(a, e, error)
                }.all { it }

fun closeTo(expected: RationalBezier, precision: Double = 1.0e-9): TypeSafeMatcher<RationalBezier> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

