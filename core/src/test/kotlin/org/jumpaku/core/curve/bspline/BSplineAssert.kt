package org.jumpaku.core.curve.bspline

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.knotAssertThat
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


fun bSplineAssertThat(actual: BSpline): BSplineAssert = BSplineAssert(actual)

class BSplineAssert(actual: BSpline) : AbstractAssert<BSplineAssert, BSpline>(actual, BSplineAssert::class.java) {

    fun isEqualToBSpline(expected: BSpline, eps: Double = 1.0e-10): BSplineAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("controlPoints size").isEqualTo(expected.controlPoints.size())

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bSpline.controlPoints[%d]", i).isEqualToPoint(e, eps)
                }

        Assertions.assertThat(actual.knots.size()).`as`("knots size").isEqualTo(expected.knots.size())

        actual.knots.zip(expected.knots)
                .forEachIndexed {
                    i, (a, e) -> knotAssertThat(a).`as`("bSpline.knots[%d]", i).isEqualToKnot(e, eps)
                }

        return this
    }
}