package jumpaku.fsc.snap

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.withPrecision

fun gridPointAssertThat(actual: GridPoint): GridPointAssert = GridPointAssert(actual)
class GridPointAssert(actual: GridPoint) : AbstractAssert<GridPointAssert, GridPoint>(actual, GridPointAssert::class.java) {

    fun isEqualToGridPoint(expected: GridPoint, eps: Double = 1.0e-10): GridPointAssert {
        isNotNull

        Assertions.assertThat(actual.x).isEqualTo(expected.x)
        Assertions.assertThat(actual.y).isEqualTo(expected.y)
        Assertions.assertThat(actual.z).isEqualTo(expected.z)

        return this
    }
}