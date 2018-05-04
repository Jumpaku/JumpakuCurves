package jumpaku.core.testold.affine

import jumpaku.core.affine.Point
import jumpaku.core.affine.WeightedPoint
import jumpaku.core.affine.weighted
import jumpaku.core.json.parseJson
import org.assertj.core.api.Assertions.*
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
        weightedPointAssertThat(wp.toString().parseJson().flatMap { WeightedPoint.fromJson(it) }.get()).isEqualToWeightedPoint(wp)
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

    @Test
    fun testWeighted() {
        println("Point.weighted")
        val a = Point.xyzr(1.0, 2.0, 3.0, 4.0).weighted(-0.4)
        val e = WeightedPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0), -0.4)
        weightedPointAssertThat(a).isEqualToWeightedPoint(e)
    }
}