package org.jumpaku.curve.bspline

import org.assertj.core.api.AbstractAssert
import org.jumpaku.affine.pointAssertThat
import org.jumpaku.curve.knotAssertThat
import org.jumpaku.util.component1
import org.jumpaku.util.component2


fun bSplineAssertThat(actual: BSpline): BSplineAssert = BSplineAssert(actual)

class BSplineAssert(actual: BSpline) : AbstractAssert<BSplineAssert, BSpline>(actual, BSplineAssert::class.java) {

    companion object{
        fun assertThat(actual: BSpline): BSplineAssert = BSplineAssert(actual)
    }

    fun isEqualToBSpline(expected: BSpline): BSplineAssert {
        isNotNull

        if (actual.controlPoints.size() != expected.controlPoints.size()){
            failWithMessage("Expected controlPoints size to be <%s> but was <%s>", expected.controlPoints.size(), actual.controlPoints.size())
            return this
        }

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bSpline.controlPoints[%d]", i).isEqualToPoint(e)
                }

        if (actual.knots.size() != expected.knots.size()){
            failWithMessage("Expected knots size to be <%s> but was <%s>", expected.controlPoints.size(), actual.controlPoints.size())
            return this
        }

        actual.knots.zip(expected.knots)
                .forEachIndexed {
                    i, (a, e) -> knotAssertThat(a).`as`("bSpline.knots[%d]", i).isEqualToKnot(e)
                }

        return this
    }
}