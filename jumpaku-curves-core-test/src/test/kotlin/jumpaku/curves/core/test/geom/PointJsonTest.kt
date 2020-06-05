package jumpaku.curves.core.test.geom

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.PointJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class PointJsonTest {

    val f = Point.xyzr(1.0, -2.0, 3.0, 2.0)
    val c = Point.xyz(1.0, -2.0, 3.0)

    @Test
    fun testPointJson() {
        println("PointJson")
        Assert.assertThat(PointJson.toJsonStr(f).parseJson().let { PointJson.fromJson(it) }, Matchers.`is`(closeTo(f)))
        Assert.assertThat(PointJson.toJsonStr(c).parseJson().let { PointJson.fromJson(it) }, Matchers.`is`(closeTo(c)))
    }

}