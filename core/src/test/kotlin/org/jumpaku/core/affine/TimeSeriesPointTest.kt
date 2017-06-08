package org.jumpaku.core.affine

import org.assertj.core.api.Assertions.*
import org.junit.Test

class TimeSeriesPointTest {
    @Test
    fun testDivide() {
        println("Divide")
        val f0 = TimeSeriesPoint(Point.xr(1.0, 10.0), 1.0)
        val f2 = TimeSeriesPoint(Point.xr(2.0, 20.0), 2.0)

        timeSeriesDataAssertThat(f0.divide( 0.3, f2)).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xr(1.3, 13.0), 1.3))
        timeSeriesDataAssertThat(f0.divide(-1.0, f2)).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xr(0.0, 40.0), 0.0))
        timeSeriesDataAssertThat(f0.divide( 2.0, f2)).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xr(3.0, 50.0), 3.0))
        timeSeriesDataAssertThat(f0.divide( 0.0, f2)).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xr(1.0, 10.0), 1.0))
        timeSeriesDataAssertThat(f0.divide( 1.0, f2)).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xr(2.0, 20.0), 2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = TimeSeriesPoint(Point.xr(1.0, 10.0), 1.0)
        timeSeriesDataAssertThat(TimeSeriesPointJson.fromJson(TimeSeriesPointJson.toJson(t)).get()).isEqualToTimeSeriesData(t)

        assertThat(TimeSeriesPointJson.fromJson("""{"point":"x": 405.0,"y": 319.0,"z": 0.0,"r": 0.0},"time": 40683.649914965004}""").isEmpty).isTrue()
        assertThat(TimeSeriesPointJson.fromJson("""{"point":null,"time": 40683.649914965004}""").isEmpty).isTrue()
        assertThat(TimeSeriesPointJson.fromJson("""point":null,"time": 40683.649914965004}""").isEmpty).isTrue()


    }

}