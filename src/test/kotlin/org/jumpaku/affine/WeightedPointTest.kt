package org.jumpaku.affine

import org.assertj.core.api.Assertions.*
import org.jumpaku.jsonAssertThat
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/17.
 */
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
        weightedPointAssertThat(WeightedPoint.fromJson("""{"point":{"x":1.0, "y":2.0, "z":3.0, "r":4.0}, "weight":-0.4}""")!!).isEqualToWeightedPoint(wp)
        jsonAssertThat(WeightedPoint.toJson(wp)).isEqualToWithoutWhitespace(
                """{"point":{"x":1.0, "y":2.0, "z":3.0, "r":4.0}, "weight":-0.4}""")
        jsonAssertThat(wp.toString()).isEqualToWithoutWhitespace(
                """{"point":{"x":1.0, "y":2.0, "z":3.0, "r":4.0}, "weight":-0.4}""")

        assertThat(WeightedPoint.fromJson("""{"point":null, "weight":-0.4}""")).isNull()
        assertThat(WeightedPoint.fromJson("""{"point":{"x":1.0, "y":2.0, "z":3.0, "r":4.0} "weight":-0.4}""")).isNull()
        assertThat(WeightedPoint.fromJson("""{"point":{"x":1.0, "y":2.0, "z":3.0, "r":4.0}, "weight":-0.4""")).isNull()
    }

    @Test
    fun testDivide() {
        println("Divide")
        val p1 = WeightedPoint(Point.xr( 2.0, 10.0), 3.0)
        val p2 = WeightedPoint(Point.xr(-2.0, 20.0), 2.0)
        weightedPointAssertThat(p1.divide(-1.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr( 16.0 / 4.0, 100.0 / 4.0), 4.0))
        weightedPointAssertThat(p1.divide( 0.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(  6.0 / 3.0,  30.0 / 3.0), 3.0))
        weightedPointAssertThat(p1.divide( 0.4, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(  2.0 / 2.6,  34.0 / 2.6), 2.6))
        weightedPointAssertThat(p1.divide( 1.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr( -4.0 / 2.0,  40.0 / 2.0), 2.0))
        weightedPointAssertThat(p1.divide( 2.0, p2)).isEqualToWeightedPoint(WeightedPoint(Point.xr(-14.0 / 1.0, 110.0 / 1.0), 1.0))
    }
}