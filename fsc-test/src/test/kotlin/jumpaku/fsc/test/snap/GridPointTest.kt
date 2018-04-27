package jumpaku.fsc.test.snap

import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.GridPoint
import org.assertj.core.api.Assertions.*
import org.junit.Test

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
