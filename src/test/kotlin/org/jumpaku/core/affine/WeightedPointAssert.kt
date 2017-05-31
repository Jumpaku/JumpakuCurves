package org.jumpaku.affine

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert

/**
 * Created by jumpaku on 2017/05/17.
 */

fun weightedPointAssertThat(actual: WeightedPoint): WeightedPointAssert = WeightedPointAssert(actual)

class WeightedPointAssert(actual: WeightedPoint) : AbstractAssert<WeightedPointAssert, WeightedPoint>(actual, WeightedPointAssert::class.java) {
    companion object{
        fun assertThat(actual: WeightedPoint): WeightedPointAssert = WeightedPointAssert(actual)
    }

    fun isEqualToWeightedPoint(expected: WeightedPoint): WeightedPointAssert {
        isNotNull

        if (!Precision.equals(actual.weight, expected.weight, 1.0e-10)){
            failWithMessage("Expected weight of weighted point to be <%s> but was <%s>", expected.weight, actual.weight)
            return this
        }

        pointAssertThat(actual.point).`as`("point of weighted point").isEqualToPoint(expected.point)

        return this
    }
}