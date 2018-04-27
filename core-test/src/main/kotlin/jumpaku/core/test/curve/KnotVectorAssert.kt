package jumpaku.core.test.curve

import jumpaku.core.curve.KnotVector
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun knotVectorAssertThat(actual: KnotVector): KnotVectorAssert = KnotVectorAssert(actual)
class KnotVectorAssert(actual: KnotVector) : AbstractAssert<KnotVectorAssert, KnotVector>(actual, KnotVectorAssert::class.java) {
    fun isEqualToKnotVector(expected: KnotVector, eps: Double = 1.0e-10): KnotVectorAssert {
        isNotNull

        Assertions.assertThat(actual.degree).isEqualTo(expected.degree)
        Assertions.assertThat(actual.knots.size()).isEqualTo(expected.knots.size())
        actual.knots.zip(expected.knots).forEachIndexed { index, (a, e) ->
            Assertions.assertThat(a.value).`as`("knot[%d]", index).isEqualTo(e.value, Assertions.withPrecision(eps))
            Assertions.assertThat(a.multiplicity).`as`("knot[%d]", index).isEqualTo(e.multiplicity)
        }

        return this
    }
}