package jumpaku.core.curve.rationalbezier

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import jumpaku.core.affine.weightedPointAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2

/**
 * Created by jumpaku on 2017/05/18.
 */

fun rationalBezierAssertThat(actual: RationalBezier): RationalBezierAssert = RationalBezierAssert(actual)

class RationalBezierAssert(actual: RationalBezier) : AbstractAssert<RationalBezierAssert, RationalBezier>(actual, RationalBezierAssert::class.java) {

    fun isEqualToRationalBezier(expected: RationalBezier, eps: Double = 1.0e-10): RationalBezierAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("size of control points of rational bezier").isEqualTo(expected.controlPoints.size())

        actual.weightedControlPoints.zip(expected.weightedControlPoints)
                .forEachIndexed {
                    i, (a, e) -> weightedPointAssertThat(a).`as`("rationalBezier.weightedControlPoints[%d]", i).isEqualToWeightedPoint(e, eps)
                }

        return this
    }
}