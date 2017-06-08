package org.jumpaku.core.curve.rationalbezier

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.jumpaku.core.affine.pointAssertThat


fun interpolatingConicSectionAssertThat(actual: InterpolatingConicSection): InterpolatingConicSectionAssert = InterpolatingConicSectionAssert(actual)

class InterpolatingConicSectionAssert(actual: InterpolatingConicSection) : AbstractAssert<InterpolatingConicSectionAssert, InterpolatingConicSection>(actual, InterpolatingConicSectionAssert::class.java) {

    fun isEqualToInterpolatingConicSection(expected: InterpolatingConicSection, eps: Double = 1.0e-10): InterpolatingConicSectionAssert {
        isNotNull

        pointAssertThat(actual.begin).`as`("begin").isEqualToPoint(expected.begin, eps)
        pointAssertThat(actual.middle).`as`("middle").isEqualToPoint(expected.middle, eps)
        pointAssertThat(actual.end).`as`("end").isEqualToPoint(expected.end, eps)
        Assertions.assertThat(actual.weight).`as`("weight").isEqualTo(expected.weight, Assertions.withPrecision(eps))

        return this
    }
}
