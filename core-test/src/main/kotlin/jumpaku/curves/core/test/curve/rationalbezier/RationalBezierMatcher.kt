package jumpaku.curves.core.test.curve.rationalbezier

import jumpaku.curves.core.curve.rationalbezier.RationalBezier
import jumpaku.curves.core.test.geom.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: RationalBezier, expected: RationalBezier, error: Double = 1.0e-9): Boolean =
        (actual.weightedControlPoints.size == expected.weightedControlPoints.size) &&
                actual.weightedControlPoints.zip(expected.weightedControlPoints) { a, e ->
                    isCloseTo(a, e, error)
                }.all { it }

fun RationalBezier.shouldEqualToRationalBezier(expected: RationalBezier, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
