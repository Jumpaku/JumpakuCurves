package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.point.PointSnapResult
import jumpaku.curves.fsc.snap.point.PointSnapResultJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class PointSnapResultJsonTest{

    @Test
    fun testPointSnapResultJson() {
        println("PointSnapResultJson")
        val a = PointSnapResult(5, GridPoint(1, 2, -8), Grade(0.6))
        val e = PointSnapResultJson.toJsonStr(a).parseJson().let { PointSnapResultJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(closeTo(e)))
    }
}