package jumpaku.fsc.test.snap

import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.test.affine.vectorAssertThat
import jumpaku.fsc.snap.Grid
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun gridAssertThat(actual: Grid): GridAssert = GridAssert(actual)
class GridAssert(actual: Grid) : AbstractAssert<GridAssert, Grid>(actual, GridAssert::class.java) {

    fun isEqualToGrid(expected: Grid, eps: Double = 1.0e-10): GridAssert {
        isNotNull

        Assertions.assertThat(actual.spacing).isEqualTo(expected.spacing, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.magnification).isEqualTo(expected.magnification)
        pointAssertThat(actual.origin).isEqualToPoint(expected.origin, eps)
        vectorAssertThat(actual.axis).isEqualToVector(expected.axis, eps)
        Assertions.assertThat(actual.radian).isEqualTo(expected.radian, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.fuzziness).isEqualTo(expected.fuzziness, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.resolution).isEqualTo(expected.resolution)

        return this
    }
}