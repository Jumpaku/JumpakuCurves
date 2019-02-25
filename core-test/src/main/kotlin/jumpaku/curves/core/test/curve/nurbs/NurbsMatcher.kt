
package jumpaku.core.test.curve.nurbs

import jumpaku.core.curve.nurbs.Nurbs
import jumpaku.core.test.curve.isCloseTo
import jumpaku.core.test.geom.isCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.amshove.kluent.should

fun isCloseTo(actual: Nurbs, expected: Nurbs, error: Double = 1.0e-9): Boolean =
        isCloseTo(actual.knotVector, expected.knotVector, error) &&
                actual.weightedControlPoints.zip(expected.weightedControlPoints).all { (a, e) -> isCloseTo(a, e, error) }

fun Nurbs.shouldEqualToNurbs(expected: Nurbs, error: Double = 1.0e-9) = this.should("$this should be $expected") {
    isCloseTo(this, expected, error)
}