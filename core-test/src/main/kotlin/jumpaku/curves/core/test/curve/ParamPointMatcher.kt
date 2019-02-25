package jumpaku.core.test.curve

import jumpaku.core.curve.ParamPoint
import org.amshove.kluent.should

fun isCloseTo(actual: ParamPoint, expected: ParamPoint, error: Double = 1.0e-9): Boolean =
        jumpaku.core.test.geom.isCloseTo(actual.point, expected.point, error) &&
                jumpaku.core.test.isCloseTo(actual.param, expected.param, error)

fun ParamPoint.shouldEqualToParamPoint(expected: ParamPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}