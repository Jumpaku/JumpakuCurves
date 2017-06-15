package org.jumpaku.core.fitting

import io.vavr.API.Array
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.timeSeriesDataAssertThat
import org.jumpaku.core.curve.Interval
import org.junit.Test



class ParameterizationTest {
    @Test
    fun testChordalParametrize() {
        println("ChordalParametrize")
        val data = chordalParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)), Interval(1.0, 10.0))
        assertThat(data.size()).isEqualTo(3)
        timeSeriesDataAssertThat(data[0]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr(-1.0, 2.0, 3.0), 1.0))
        timeSeriesDataAssertThat(data[1]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr( 2.0,-2.0, 2.0), 6.0))
        timeSeriesDataAssertThat(data[2]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr( 2.0, 2.0, 1.0), 10.0))
    }

    @Test
    fun testUniformParametrize() {
        println("UniformParametrize")
        val data = uniformParametrize(Array(Point.xyr(-1.0, 2.0, 3.0), Point.xyr(2.0, -2.0, 2.0), Point.xyr(2.0, 2.0, 1.0)), Interval(1.0, 10.0))
        assertThat(data.size()).isEqualTo(3)
        timeSeriesDataAssertThat(data[0]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr(-1.0, 2.0, 3.0), 1.0))
        timeSeriesDataAssertThat(data[1]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr( 2.0,-2.0, 2.0), 5.5))
        timeSeriesDataAssertThat(data[2]).isEqualToTimeSeriesData(TimeSeriesPoint(Point.xyr( 2.0, 2.0, 1.0), 10.0))
    }
}

