package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.test.matcher
import org.amshove.kluent.should
import org.hamcrest.TypeSafeMatcher

fun isCloseTo(actual: WeightedPoint, expected: WeightedPoint, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.point, expected.point, error) &&
                jumpaku.curves.core.test.isCloseTo(actual.weight, expected.weight, error)

fun closeTo(expected: WeightedPoint, precision: Double = 1.0e-9): TypeSafeMatcher<WeightedPoint> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

fun WeightedPoint.shouldEqualToWeightedPoint(expected: WeightedPoint, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
