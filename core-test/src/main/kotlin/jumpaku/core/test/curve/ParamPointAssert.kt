package jumpaku.core.test.curve

import jumpaku.core.curve.ParamPoint
import jumpaku.core.test.affine.pointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun paramPointAssertThat(actual: ParamPoint): ParamPointAssert = ParamPointAssert(actual)
class ParamPointAssert(actual: ParamPoint) : AbstractAssert<ParamPointAssert, ParamPoint>(actual, ParamPointAssert::class.java) {

    fun isEqualToParamPoint(expected: ParamPoint, eps: Double = 1.0e-10): ParamPointAssert {
        isNotNull

        pointAssertThat(actual.point).`as`("point of parametrized point").isEqualToPoint(expected.point, eps)

        Assertions.assertThat(actual.param).`as`("parameter of parametrized point")
                .isEqualTo(expected.param, Assertions.withPrecision(eps))

        return this
    }
}