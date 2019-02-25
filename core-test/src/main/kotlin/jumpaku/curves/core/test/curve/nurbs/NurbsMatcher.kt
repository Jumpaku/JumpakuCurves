
package jumpaku.curves.core.test.curve.nurbs

import jumpaku.curves.core.curve.nurbs.Nurbs
import jumpaku.curves.core.test.curve.isCloseTo
import jumpaku.curves.core.test.geom.isCloseTo
import org.amshove.kluent.should

fun isCloseTo(actual: Nurbs, expected: Nurbs, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.weightedControlPoints.zip(expected.weightedControlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun Nurbs.shouldEqualToNurbs(expected: Nurbs, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}