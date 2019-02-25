package jumpaku.curves.core.test.curve.bspline

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.core.test.geom.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: BSpline, expected: BSpline, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.controlPoints.zip(expected.controlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun BSpline.shouldEqualToBSpline(expected: BSpline, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}