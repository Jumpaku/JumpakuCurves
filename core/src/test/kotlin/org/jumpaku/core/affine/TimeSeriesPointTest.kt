package org.jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.json.prettyGson
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
        timeSeriesDataAssertThat(prettyGson.fromJson<TimeSeriesPointJson>(t.toString()).timeSeriesPoint()).isEqualToTimeSeriesData(t)
    }

}