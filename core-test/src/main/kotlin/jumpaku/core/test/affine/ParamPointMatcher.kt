package jumpaku.core.test.affine

import jumpaku.core.geom.ParamPoint
import org.amshove.kluent.should

fun isCloseTo(actual: ParamPoint, expected: ParamPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                jumpaku.core.test.isCloseTo(actual.param, expected.param, error)

fun ParamPoint.shouldEqualToParamPoint(expected: ParamPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}