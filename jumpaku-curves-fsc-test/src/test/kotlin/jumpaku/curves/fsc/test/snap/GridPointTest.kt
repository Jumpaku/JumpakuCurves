package jumpaku.curves.fsc.test.snap

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.GridPointJson
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class GridPointJsonTest {

    val gp = GridPoint(4, -3, 0)

    @Test
    fun testGridPointJson() {
        println("GridPointJson")
        assertThat(GridPointJson.toJsonStr(gp).parseJson().let { GridPointJson.fromJson(it) }, `is`(equalTo(gp)))
    }
}