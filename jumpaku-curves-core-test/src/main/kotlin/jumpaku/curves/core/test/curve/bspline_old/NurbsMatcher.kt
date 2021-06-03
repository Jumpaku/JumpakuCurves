package jumpaku.curves.core.test.curve.bspline_old

import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.bspline_old.Nurbs
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: Nurbs, expected: Nurbs, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.weightedControlPoints.zip(expected.weightedControlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun closeTo(expected: Nurbs, precision: Double = 1.0e-9): TypeSafeMatcher<Nurbs> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

