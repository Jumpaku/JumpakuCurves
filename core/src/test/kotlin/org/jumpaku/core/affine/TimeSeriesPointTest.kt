package org.jumpaku.core.affine

import org.assertj.core.api.Assertions.*
import org.junit.Test

class TimeSeriesPointTest {
    @Test
    fun testDivide() {
        println("Divide")
        val f0 = TimeSeriesPoint(Point.xr(1.0, 10.0), 1.0)
        val f2 = TimeSeriesPoint(Point.xr(2.0, 20.0), 2.0)

        pointAssertThat(f0.divide( 0.3, f2).point).isEqualToPoint(Point.xr(1.3, 13.0))
        pointAssertThat(f0.divide(-1.0, f2).point).isEqualToPoint(Point.xr(0.0, 40.0))
        pointAssertThat(f0.divide( 2.0, f2).point).isEqualToPoint(Point.xr(3.0, 50.0))
        pointAssertThat(f0.divide( 0.0, f2).point).isEqualToPoint(Point.xr(1.0, 10.0))
        pointAssertThat(f0.divide( 1.0, f2).point).isEqualToPoint(Point.xr(2.0, 20.0))
        assertThat(f0.divide( 0.3, f2).time).isEqualTo(1.3, withPrecision(1.0e-10))
        assertThat(f0.divide(-1.0, f2).time).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(f0.divide( 2.0, f2).time).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(f0.divide( 0.0, f2).time).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(f0.divide( 1.0, f2).time).isEqualTo(2.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = TimeSeriesPoint(Point.xr(1.0, 10.0), 1.0)
        pointAssertThat(TimeSeriesPointJson.fromJson(t.toString()).get().point).isEqualToPoint(t.point)
        assertThat(TimeSeriesPointJson.fromJson(t.toString()).get().time).isEqualTo(1.0, withPrecision(1.0e-10))
        pointAssertThat(TimeSeriesPointJson.fromJson(TimeSeriesPointJson.toJson(t)).get().point).isEqualToPoint(t.point)
        assertThat(TimeSeriesPointJson.fromJson(TimeSeriesPointJson.toJson(t)).get().time).isEqualTo(1.0, withPrecision(1.0e-10))

        assertThat(TimeSeriesPointJson.fromJson("""{"point":"x": 405.0,"y": 319.0,"z": 0.0,"r": 0.0},"time": 40683.649914965004}""").isEmpty).isTrue()
        assertThat(TimeSeriesPointJson.fromJson("""{"point":null,"time": 40683.649914965004}""").isEmpty).isTrue()
        assertThat(TimeSeriesPointJson.fromJson("""point":null,"time": 40683.649914965004}""").isEmpty).isTrue()


    }

}