package org.jumpaku.core.curve.rationalrationalBezier

import org.assertj.core.api.AbstractAssert
import org.jumpaku.core.affine.weightedPointAssertThat
import org.jumpaku.core.curve.rationalbezier.RationalBezier
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2

/**
 * Created by jumpaku on 2017/05/18.
 */

fun rationalBezierAssertThat(actual: RationalBezier): RationalBezierAssert = RationalBezierAssert(actual)

class RationalBezierAssert(actual: RationalBezier) : AbstractAssert<RationalBezierAssert, RationalBezier>(actual, RationalBezierAssert::class.java) {

    companion object{
        fun assertThat(actual: RationalBezier): RationalBezierAssert = RationalBezierAssert(actual)
    }

    fun isEqualToRationalBezier(expected: RationalBezier): RationalBezierAssert {
        isNotNull

        if (actual.controlPoints.size() != expected.controlPoints.size()){
            failWithMessage("Expected rationalBezier size to be <%s> but was <%s>", expected.controlPoints.size(), actual.controlPoints.size())
            return this
        }

        actual.weightedControlPoints.zip(expected.weightedControlPoints)
                .forEachIndexed {
                    i, (a, e) -> weightedPointAssertThat(a).`as`("rationalBezier.weightedControlPoints[%d]", i).isEqualToWeightedPoint(e)
                }

        return this
    }
}