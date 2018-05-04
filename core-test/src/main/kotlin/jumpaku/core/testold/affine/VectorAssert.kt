package jumpaku.core.testold.affine

import jumpaku.core.affine.Vector
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun vectorAssertThat(actual: Vector): VectorAssert = VectorAssert(actual)
class VectorAssert(actual: Vector) : AbstractAssert<VectorAssert, Vector>(actual, VectorAssert::class.java) {

    fun isEqualToVector(expected: Vector, eps: Double = 1.0e-10): VectorAssert {
        isNotNull

        Assertions.assertThat(actual.x).`as`("x of point").isEqualTo(expected.x, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.y).`as`("y of point").isEqualTo(expected.y, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.z).`as`("z of point").isEqualTo(expected.z, Assertions.withPrecision(eps))

        return this
    }
}