package jumpaku.core.test.fit

import jumpaku.core.fit.WeightedParamPoint
import jumpaku.core.test.affine.pointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun weightedParamPointAssertThat(actual: WeightedParamPoint): WeightedParamPointAssert = WeightedParamPointAssert(actual)
class WeightedParamPointAssert(actual: WeightedParamPoint) : AbstractAssert<WeightedParamPointAssert, WeightedParamPoint>(actual, WeightedParamPointAssert::class.java) {

    fun isEqualToWeightedParamPoint(expected: WeightedParamPoint, eps: Double = 1.0e-10): WeightedParamPointAssert {
        isNotNull

        pointAssertThat(actual.point).`as`("point of weighted parametrized point").isEqualToPoint(expected.point, eps)

        Assertions.assertThat(actual.param).`as`("parameter of weighted parametrized point")
                .isEqualTo(expected.param, Assertions.withPrecision(eps))

        Assertions.assertThat(actual.weight).`as`("weight of weighted parametrized point")
                .isEqualTo(expected.weight, Assertions.withPrecision(eps))

        return this
    }
}