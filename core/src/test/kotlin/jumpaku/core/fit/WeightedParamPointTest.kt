package jumpaku.core.fit

import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.paramPointAssertThat
import jumpaku.core.json.parseToJson
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
        weightedParamPointAssertThat(w.toString().parseToJson().get().weightedParamPoint).isEqualToWeightedParamPoint(w)
    }
}