package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.ParamPoint
import org.amshove.kluent.should

fun isCloseTo(actual: ParamPoint, expected: ParamPoint, error: Double = 1.0e-9): Boolean =
        jumpaku.curves.core.test.geom.isCloseTo(actual.point, expected.point, error) &&
                jumpaku.curves.core.test.isCloseTo(actual.param, expected.param, error)

fun ParamPoint.shouldEqualToParamPoint(expected: ParamPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}