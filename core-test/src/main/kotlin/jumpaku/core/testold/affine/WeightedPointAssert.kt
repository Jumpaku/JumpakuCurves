package jumpaku.core.testold.affine

import jumpaku.core.affine.WeightedPoint
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

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