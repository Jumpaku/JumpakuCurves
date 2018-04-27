package jumpaku.core.test.fit

import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.curve.ParamPoint
import jumpaku.core.fit.WeightedParamPoint
import jumpaku.core.fit.weighted
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.test.curve.paramPointAssertThat
import org.junit.Test

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
        weightedParamPointAssertThat(w.toString().parseJson().flatMap { WeightedParamPoint.fromJson(it) }.get()).isEqualToWeightedParamPoint(w)
    }
}