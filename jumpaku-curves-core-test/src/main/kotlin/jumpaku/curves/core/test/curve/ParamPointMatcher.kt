package jumpaku.curves.core.test.curve

import jumpaku.commons.test.matcher
import jumpaku.commons.test.math.isCloseTo
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.test.geom.isCloseTo
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: ParamPoint, expected: ParamPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                isCloseTo(actual.param, expected.param, error)

fun closeTo(expected: ParamPoint, precision: Double = 1.0e-9): TypeSafeMatcher<ParamPoint> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

