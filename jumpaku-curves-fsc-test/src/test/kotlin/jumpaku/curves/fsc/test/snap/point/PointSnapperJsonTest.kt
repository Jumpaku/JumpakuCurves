package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.point.IFGS
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapper
import jumpaku.curves.fsc.snap.point.PointSnapperJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapperJsonTest {

    val mfgs = MFGS(-1, 1)
    val ifgs = IFGS

    @Test
    fun testPointSnapperJson() {
        println("PointSnapperJson")
        val a0 = PointSnapperJson.toJsonStr(mfgs).parseJson().let { PointSnapperJson.fromJson(it) }
        assertThat(a0, `is`(equalTo(mfgs)))
        val a1 = PointSnapperJson.toJsonStr(ifgs).parseJson().let { PointSnapperJson.fromJson(it) }
        assertThat(a1, `is`(equalTo(ifgs)))
    }
}