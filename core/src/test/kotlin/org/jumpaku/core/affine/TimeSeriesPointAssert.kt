package org.jumpaku.core.affine

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions


fun timeSeriesDataAssertThat(actual: TimeSeriesPoint): TimeSeriesPointAssert = TimeSeriesPointAssert(actual)

class TimeSeriesPointAssert(actual: TimeSeriesPoint) : AbstractAssert<TimeSeriesPointAssert, TimeSeriesPoint>(actual, TimeSeriesPointAssert::class.java) {

    fun isEqualToTimeSeriesData(expected: TimeSeriesPoint, eps: Double = 1.0e-10): TimeSeriesPointAssert {
        isNotNull

        pointAssertThat(actual.point).`as`("point of time series point").isEqualToPoint(expected.point, eps)

        Assertions.assertThat(actual.time).`as`("time of time series point")
                .isEqualTo(expected.time, Assertions.withPrecision(eps))

        return this
    }
}
