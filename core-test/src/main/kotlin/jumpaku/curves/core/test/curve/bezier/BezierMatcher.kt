package jumpaku.curves.core.test.curve.bezier

import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.test.geom.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Bezier, expected: Bezier, error: Double = 1.0e-9): Boolean =
        (actual.controlPoints.size == expected.controlPoints.size) &&
                actual.controlPoints.zip(expected.controlPoints) { a, e -> isCloseTo(a, e, error) }.all { it }


fun Bezier.shouldEqualToBezier(expected: Bezier, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}
