package org.jumpaku.core.curve

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions


fun knotAssertThat(actual: Knot): KnotAssert = KnotAssert(actual)

class KnotAssert(actual: Knot) : AbstractAssert<KnotAssert, Knot>(actual, KnotAssert::class.java) {
    fun isEqualToKnot(expected: Knot): KnotAssert {
        isNotNull

        Assertions.assertThat(actual.value).`as`("value of knot")
                .isEqualTo(expected.value, Assertions.withPrecision(1.0e-10))
        Assertions.assertThat(actual.multiplicity).`as`("multiplicity of knot")
                .isEqualTo(expected.multiplicity)

        return this
    }
}