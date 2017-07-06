package org.jumpaku.fsc.identify.reference

import org.assertj.core.api.AbstractAssert
import org.jumpaku.core.curve.intervalAssertThat
import org.jumpaku.core.curve.rationalbezier.lineSegmentAssertThat
import org.jumpaku.fsc.identify.reference.Linear


fun linearAssertThat(actual: Linear): LinearAssert = LinearAssert(actual)

class LinearAssert(actual: Linear) : AbstractAssert<LinearAssert, Linear>(actual, LinearAssert::class.java) {
    fun isEqualToLinear(expected: Linear, eps: Double = 1.0e-10): LinearAssert {
        isNotNull

        lineSegmentAssertThat(actual.lineSegment).isEqualLineSegment(expected.lineSegment, eps)

        return this
    }
}