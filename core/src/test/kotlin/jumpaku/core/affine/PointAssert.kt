package jumpaku.core.affine

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

/**
 * Created by jumpaku on 2017/05/10.
 */

fun pointAssertThat(actual: Point): PointAssert = PointAssert(actual)

class PointAssert(actual: Point) : AbstractAssert<PointAssert, Point>(actual, PointAssert::class.java) {

    fun isEqualToPoint(expected: Point, eps: Double = 1.0e-10): PointAssert {
        isNotNull

        Assertions.assertThat(actual.r).`as`("r of point").isEqualTo(expected.r, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.x).`as`("x of point").isEqualTo(expected.x, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.y).`as`("y of point").isEqualTo(expected.y, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.z).`as`("z of point").isEqualTo(expected.z, Assertions.withPrecision(eps))

        return this
    }
}
