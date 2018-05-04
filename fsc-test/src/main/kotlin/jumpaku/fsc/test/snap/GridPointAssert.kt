package jumpaku.fsc.test.snap

import jumpaku.fsc.snap.GridPoint
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun gridPointAssertThat(actual: GridPoint): GridPointAssert = GridPointAssert(actual)
class GridPointAssert(actual: GridPoint) : AbstractAssert<GridPointAssert, GridPoint>(actual, GridPointAssert::class.java) {

    fun isEqualToGridPoint(expected: GridPoint): GridPointAssert {
        isNotNull

        Assertions.assertThat(actual.x).isEqualTo(expected.x)
        Assertions.assertThat(actual.y).isEqualTo(expected.y)
        Assertions.assertThat(actual.z).isEqualTo(expected.z)

        return this
    }
}