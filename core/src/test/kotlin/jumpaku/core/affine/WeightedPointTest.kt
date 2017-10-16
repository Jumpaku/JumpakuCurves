package jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.*
import jumpaku.core.json.prettyGson
import org.junit.Test


class WeightedPointTest {

    @Test
    fun testProperties() {
        println("Properties")
        val wp = WeightedPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0), -0.4)
        assertThat(wp.weight).isEqualTo(-0.4, withPrecision(1.0e-10))
        pointAssertThat(wp.point).isEqualToPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val wp = WeightedPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0), -0.4)
        weightedPointAssertThat(wp.toString().parseToJson().get().weightedPoint).isEqualToWeightedPoint(wp)
    }

    @Test
    fun testDivide() {
        println("Divide")
        val p1 = WeightedPoint(Point.xr(2.0, 10.0), 3.0)
        val p2 = WeightedPoint(Point.xr(-2.0, 20.0), 2.0)
        weightedPointAssertThat(p1.divide(-1.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(16.0 / 4.0, 100.0 / 4.0), 4.0))
        weightedPointAssertThat(p1.divide(0.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(6.0 / 3.0, 30.0 / 3.0), 3.0))
        weightedPointAssertThat(p1.divide(0.4, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(2.0 / 2.6, 34.0 / 2.6), 2.6))
        weightedPointAssertThat(p1.divide(1.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(-4.0 / 2.0, 40.0 / 2.0), 2.0))
        weightedPointAssertThat(p1.divide(2.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(-14.0 / 1.0, 110.0 / 1.0), 1.0))
    }
}