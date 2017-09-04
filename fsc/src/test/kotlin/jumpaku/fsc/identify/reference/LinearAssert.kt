package jumpaku.fsc.identify.reference

import org.assertj.core.api.AbstractAssert
import jumpaku.core.curve.intervalAssertThat
import jumpaku.core.curve.rationalbezier.conicSectionAssertThat


fun linearAssertThat(actual: Linear): LinearAssert = LinearAssert(actual)

class LinearAssert(actual: Linear) : AbstractAssert<LinearAssert, Linear>(actual, LinearAssert::class.java) {
    fun isEqualToLinear(expected: Linear, eps: Double = 1.0e-10): LinearAssert {
        isNotNull

        conicSectionAssertThat(actual.conicSection).isEqualConicSection(expected.conicSection, eps)
        intervalAssertThat(actual.domain).isEqualToInterval(expected.domain, eps)
        return this
    }
}