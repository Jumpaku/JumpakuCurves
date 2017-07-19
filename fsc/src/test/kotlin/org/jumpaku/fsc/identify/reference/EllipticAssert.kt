package org.jumpaku.fsc.identify.reference

import org.assertj.core.api.AbstractAssert
import org.jumpaku.core.curve.intervalAssertThat
import org.jumpaku.core.curve.rationalbezier.conicSectionAssertThat


fun ellipticAssertThat(actual: Elliptic): EllipticAssert = EllipticAssert(actual)

class EllipticAssert(actual: Elliptic) : AbstractAssert<EllipticAssert, Elliptic>(actual, EllipticAssert::class.java) {

    fun isEqualToElliptic(expected: Elliptic, eps: Double = 1.0e-10): EllipticAssert {
        isNotNull

        conicSectionAssertThat(actual.conicSection).isEqualConicSection(expected.conicSection, eps)
        intervalAssertThat(actual.domain).isEqualToInterval(expected.domain, eps)

        return this
    }
}