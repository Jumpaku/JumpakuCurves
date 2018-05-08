package jumpaku.core.test.fit

import jumpaku.core.fit.WeightedParamPoint
import jumpaku.core.test.affine.isCloseTo
import jumpaku.core.test.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: WeightedParamPoint, expected: WeightedParamPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.paramPoint, expected.paramPoint, error) &&
                isCloseTo(actual.weight, expected.weight, error)

fun WeightedParamPoint.shouldBeWeightedPoint(expected: WeightedParamPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}