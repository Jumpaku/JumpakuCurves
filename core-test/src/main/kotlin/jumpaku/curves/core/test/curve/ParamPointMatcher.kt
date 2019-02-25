package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.core.test.isCloseTo
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: ParamPoint, expected: ParamPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                isCloseTo(actual.param, expected.param, error)

fun closeTo(expected: ParamPoint, precision: Double = 1.0e-9): TypeSafeMatcher<ParamPoint> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun ParamPoint.shouldEqualToParamPoint(expected: ParamPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}