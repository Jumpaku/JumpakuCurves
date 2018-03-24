package jumpaku.fsc.snap

import jumpaku.core.affine.Point
import jumpaku.core.json.parseJson
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

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

class GridPointTest {

    val gridPoint = GridPoint(4, -3, 0)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(gridPoint.x).isEqualTo(4)
        assertThat(gridPoint.y).isEqualTo(-3)
        assertThat(gridPoint.z).isEqualTo(0)
    }

    @Test
    fun testToString() {
        println("ToString")
        val j = gridPoint.toString().parseJson().flatMap { GridPoint.fromJson(it) }.get()
        gridPointAssertThat(j).isEqualToGridPoint(gridPoint)
    }
}
