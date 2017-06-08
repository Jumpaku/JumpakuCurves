package org.jumpaku.core.affine

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions


fun timeSeriesDataAssertThat(actual: TimeSeriesPoint): TimeSeriesPointAssert = TimeSeriesPointAssert(actual)

class TimeSeriesPointAssert(actual: TimeSeriesPoint) : AbstractAssert<TimeSeriesPointAssert, TimeSeriesPoint>(actual, TimeSeriesPointAssert::class.java) {
    companion object{
        fun assertThat(actual: TimeSeriesPoint): TimeSeriesPointAssert = TimeSeriesPointAssert(actual)
    }

    fun isEqualToTimeSeriesData(expected: TimeSeriesPoint): TimeSeriesPointAssert {
        isNotNull

        pointAssertThat(actual.point).`as`("point of time series point").isEqualToPoint(expected.point)

        Assertions.assertThat(actual.time).`as`("time of time series point")
                .isEqualTo(expected.time, Assertions.withPrecision(1.0e-10))

        return this
    }
}
