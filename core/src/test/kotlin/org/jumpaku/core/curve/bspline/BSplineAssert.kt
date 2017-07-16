package org.jumpaku.core.curve.bspline

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.knotVectorAssertThat
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

        Assertions.assertThat(actual.knotVector.size()).`as`("knotVector size").isEqualTo(expected.knotVector.size())

        knotVectorAssertThat(actual.knotVector).isEqualToKnotVector(expected.knotVector)

        return this
    }
}