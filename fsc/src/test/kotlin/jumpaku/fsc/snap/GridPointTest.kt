package jumpaku.fsc.snap

import jumpaku.core.affine.Point
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.*
import org.junit.Test

class GridPointTest {

    val baseGrid = BaseGrid(
            spacing = 4.0,
            magnification = 4,
            origin = Point.xyz(4.0, 4.0, 0.0),
            fuzziness = 2.0)

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
        val j = gridPoint.toString().parseToJson().get().gridPoint
        gridPointAssertThat(j).isEqualToGridPoint(gridPoint)
    }
}
