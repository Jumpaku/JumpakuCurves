package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapper
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapperTest {

    val snapper = MFGS(-1, 1)

    @Test
    fun testToString() {
        println("ToString")
        val a = snapper.toString().parseJson().tryMap { PointSnapper.fromJson(it) }.orThrow()
        assertThat(a, `is`(equalTo(snapper)))
    }
}