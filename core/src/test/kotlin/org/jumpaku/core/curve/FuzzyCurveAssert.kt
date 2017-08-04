package org.jumpaku.core.curve

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


fun fuzzyCurveAssertThat(actual: FuzzyCurve): FuzzyCurveAssert = FuzzyCurveAssert(actual)

class FuzzyCurveAssert(actual: FuzzyCurve) : AbstractAssert<FuzzyCurveAssert, FuzzyCurve>(actual, FuzzyCurveAssert::class.java) {

    fun isEqualToFuzzyCurve(expected: FuzzyCurve, eps: Double = 1.0e-10, sampleCount: Int = 30): FuzzyCurveAssert {
        isNotNull

        actual.evaluateAll(30).zip(expected.evaluateAll(30)).forEachIndexed { index, (ap, ep) ->
            pointAssertThat(ap.toCrisp()).`as`("point[$index]").isEqualToPoint(ep.toCrisp(), eps)
        }
        actual.evaluateAll(30).zip(expected.evaluateAll(30)).forEachIndexed { index, (ap, ep) ->
            Assertions.assertThat(ap.r).`as`("r[$index]").isEqualTo(ep.r, Assertions.withPrecision(eps))
        }

        return this
    }
}