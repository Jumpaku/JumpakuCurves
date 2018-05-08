package jumpaku.fsc.oldtest.classify

import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.ClassifyResult
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision


fun classifyResultAssertThat(actual: ClassifyResult): ClassifyResultAssert = ClassifyResultAssert(actual)

class ClassifyResultAssert(actual: ClassifyResult) : AbstractAssert<ClassifyResultAssert, ClassifyResult>(actual, ClassifyResultAssert::class.java) {

    fun isEqualToClassifyResult(expected: ClassifyResult, eps: Double = 1.0e-10): ClassifyResultAssert {
        isNotNull

        assertThat(actual.grades.size()).isEqualTo(expected.grades.size())
        actual.grades.zip(expected.grades).forEach { (a, e) ->
            assertThat(a._1).isEqualTo(e._1)
            assertThat(a._2.value).isEqualTo(e._2.value, withPrecision(eps))
        }
        assertThat(actual.curveClass).isEqualTo(expected.curveClass)
        assertThat(actual.grade.value).isEqualTo(expected.grade.value, withPrecision(eps))

        return this
    }
}