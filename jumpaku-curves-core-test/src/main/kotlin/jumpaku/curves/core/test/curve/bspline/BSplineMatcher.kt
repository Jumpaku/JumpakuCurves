package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: BSpline, expected: BSpline, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.controlPoints.zip(expected.controlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun closeTo(expected: BSpline, precision: Double = 1.0e-9): TypeSafeMatcher<BSpline> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

