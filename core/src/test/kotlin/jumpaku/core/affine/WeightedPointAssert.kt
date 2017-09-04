package jumpaku.core.affine

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

/**
 * Created by jumpaku on 2017/05/17.
 */

fun weightedPointAssertThat(actual: WeightedPoint): WeightedPointAssert = WeightedPointAssert(actual)

class WeightedPointAssert(actual: WeightedPoint) : AbstractAssert<WeightedPointAssert, WeightedPoint>(actual, WeightedPointAssert::class.java) {

    fun isEqualToWeightedPoint(expected: WeightedPoint, eps: Double = 1.0e-10): WeightedPointAssert {
        isNotNull

        Assertions.assertThat(actual.weight).`as`("weight of weighted point")
                .isEqualTo(expected.weight, Assertions.withPrecision(eps))

        pointAssertThat(actual.point).`as`("point of weighted point").isEqualToPoint(expected.point, eps)

        return this
    }
}