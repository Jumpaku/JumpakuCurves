package jumpaku.core.test.curve.bspline

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.test.affine.isCloseTo
import jumpaku.core.test.curve.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: BSpline, expected: BSpline, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.controlPoints.zip(expected.controlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun BSpline.shouldBeBSpline(expected: BSpline, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}