package jumpaku.core.fit

import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.paramPointAssertThat
import jumpaku.core.json.parseJson
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.junit.Test

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

class WeightedParamPointTest {

    @Test
    fun testProperties() {
        println("Properties")
        val w = ParamPoint(Point.xr(1.0, 10.0), 1.0).weighted(2.0)
        paramPointAssertThat(w.paramPoint).isEqualToParamPoint(ParamPoint(Point.xr(1.0, 10.0), 1.0))
        assertThat(w.weight).isEqualTo(2.0, withPrecision(1.0e-10))
        pointAssertThat(w.point).isEqualTo(Point.xr(1.0, 10.0))
        assertThat(w.param).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val w = ParamPoint(Point.xr(1.0, 10.0), 1.0).weighted(2.0)
        weightedParamPointAssertThat(w.toString().parseJson().get().weightedParamPoint).isEqualToWeightedParamPoint(w)
    }
}