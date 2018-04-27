package jumpaku.core.test.curve.rationalbezier

import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.test.affine.pointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun conicSectionAssertThat(actual: ConicSection): ConicSectionAssert = ConicSectionAssert(actual)
class ConicSectionAssert(actual: ConicSection) : AbstractAssert<ConicSectionAssert, ConicSection>(actual, ConicSectionAssert::class.java) {

    fun isEqualConicSection(expected: ConicSection, eps: Double = 1.0e-10): ConicSectionAssert {
        isNotNull

        pointAssertThat(actual.begin).`as`("begin").isEqualToPoint(expected.begin, eps)
        pointAssertThat(actual.far).`as`("far").isEqualToPoint(expected.far, eps)
        pointAssertThat(actual.end).`as`("end").isEqualToPoint(expected.end, eps)
        Assertions.assertThat(actual.weight).`as`("weight").isEqualTo(expected.weight, Assertions.withPrecision(eps))

        return this
    }
}