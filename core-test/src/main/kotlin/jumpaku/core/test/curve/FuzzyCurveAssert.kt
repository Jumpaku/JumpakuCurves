package jumpaku.core.test.curve

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import jumpaku.core.curve.FuzzyCurve
import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2


fun fuzzyCurveAssertThat(actual: FuzzyCurve): FuzzyCurveAssert = FuzzyCurveAssert(actual)

class FuzzyCurveAssert(actual: FuzzyCurve) : AbstractAssert<FuzzyCurveAssert, FuzzyCurve>(actual, FuzzyCurveAssert::class.java) {

    fun isEqualToFuzzyCurve(expected: FuzzyCurve, eps: Double = 1.0e-10, sampleCount: Int = 30): FuzzyCurveAssert {
        isNotNull

        actual.evaluateAll(sampleCount).zip(expected.evaluateAll(sampleCount)).forEachIndexed { index, (ap, ep) ->
            pointAssertThat(ap.toCrisp()).`as`("point[$index]").isEqualToPoint(ep.toCrisp(), eps)
        }
        actual.evaluateAll(sampleCount).zip(expected.evaluateAll(sampleCount)).forEachIndexed { index, (ap, ep) ->
            Assertions.assertThat(ap.r).`as`("r[$index]").isEqualTo(ep.r, Assertions.withPrecision(eps))
        }

        return this
    }
}