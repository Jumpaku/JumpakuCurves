package org.jumpaku.core.curve.rationalbezier

import org.assertj.core.api.AbstractAssert
import org.jumpaku.core.curve.paramPointAssertThat


fun lineSegmentAssertThat(actual: LineSegment): LineSegmentAssert = LineSegmentAssert(actual)

class LineSegmentAssert(actual: LineSegment) : AbstractAssert<LineSegmentAssert, LineSegment>(actual, LineSegmentAssert::class.java) {

    fun isEqualLineSegment(expected: LineSegment, eps: Double = 1.0e-10): LineSegmentAssert {
        isNotNull

        paramPointAssertThat(actual.front).`as`("front").isEqualToParamPoint(expected.front, eps)
        paramPointAssertThat(actual.back).`as`("end").isEqualToParamPoint(expected.back, eps)

        return this
    }
}
