package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapper
import jumpaku.curves.fsc.snap.point.PointSnapperJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapperJsonTest {

    val snapper = MFGS(-1, 1)

    @Test
    fun testPointSnapperJson() {
        println("PointSnapperJson")
        val a = PointSnapperJson.toJsonStr(snapper).parseJson().let { PointSnapperJson.fromJson(it) }
        assertThat(a, `is`(equalTo(snapper)))
    }
}