package jumpaku.curves.fsc.test.snap

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.GridPoint
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class GridPointTest {

    val gp = GridPoint(4, -3, 0)

    @Test
    fun testToString() {
        println("ToString")
        assertThat(gp.toString().parseJson().tryMap { GridPoint.fromJson(it) }.orThrow(), `is`(equalTo(gp)))
    }
}