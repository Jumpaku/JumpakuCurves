package org.jumpaku.core.curve

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions


fun intervalAssertThat(actual: Interval): IntervalAssert = IntervalAssert(actual)

class IntervalAssert(actual: Interval) : AbstractAssert<IntervalAssert, Interval>(actual, IntervalAssert::class.java) {
    fun isEqualToInterval(expected: Interval, eps: Double = 1.0e-10): IntervalAssert {
        isNotNull

        Assertions.assertThat(actual.begin).`as`("begin of interval")
                .isEqualTo(expected.begin, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.end).`as`("end of interval")
                .isEqualTo(expected.end, Assertions.withPrecision(eps))

        return this
    }
}