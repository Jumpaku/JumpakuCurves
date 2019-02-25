package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.WeightedPoint
import org.amshove.kluent.should

fun isCloseTo(actual: WeightedPoint, expected: WeightedPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                jumpaku.curves.core.test.isCloseTo(actual.weight, expected.weight, error)

fun WeightedPoint.shouldEqualToWeightedPoint(expected: WeightedPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
