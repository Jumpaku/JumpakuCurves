package org.jumpaku.curve.rationalbezier

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.jumpaku.affine.pointAssertThat


fun interpolatingConicSectionAssertThat(actual: InterpolatingConicSection): InterpolatingConicSectionAssert = InterpolatingConicSectionAssert(actual)

class InterpolatingConicSectionAssert(actual: InterpolatingConicSection) : AbstractAssert<InterpolatingConicSectionAssert, InterpolatingConicSection>(actual, InterpolatingConicSectionAssert::class.java) {

    companion object{
        fun assertThat(actual: InterpolatingConicSection): InterpolatingConicSectionAssert = InterpolatingConicSectionAssert(actual)
    }

    fun isEqualToInterpolatingConicSection(expected: InterpolatingConicSection): InterpolatingConicSectionAssert {
        isNotNull

        pointAssertThat(actual.begin).`as`("begin").isEqualToPoint(expected.begin)
        pointAssertThat(actual.middle).`as`("middle").isEqualToPoint(expected.middle)
        pointAssertThat(actual.end).`as`("end").isEqualToPoint(expected.end)
        Assertions.assertThat(actual.weight).`as`("weight").isEqualTo(expected.weight, Assertions.withPrecision(1.0e-10))

        return this
    }
}
