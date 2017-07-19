package org.jumpaku.fsc.identify.reference

import org.assertj.core.api.AbstractAssert
import org.jumpaku.core.curve.intervalAssertThat
import org.jumpaku.core.curve.rationalbezier.conicSectionAssertThat


fun circularAssertThat(actual: Circular): CircularAssert = CircularAssert(actual)

class CircularAssert(actual: Circular) : AbstractAssert<CircularAssert, Circular>(actual, CircularAssert::class.java) {

    fun isEqualToCircular(expected: Circular, eps: Double = 1.0e-10): CircularAssert {
        isNotNull

        conicSectionAssertThat(actual.conicSection).isEqualConicSection(expected.conicSection, eps)
        intervalAssertThat(actual.domain).isEqualToInterval(expected.domain, eps)

        return this
    }
}