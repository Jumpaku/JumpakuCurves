package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.transform.UniformlyScaleJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class UniformlyScaleJsonTest {

    val t = UniformlyScale(2.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testToString() {
        println("ToString")
        val a = UniformlyScaleJson.toJsonStr(t).parseJson().let { UniformlyScaleJson.fromJson(it) }
        Assert.assertThat(a(p), Matchers.`is`(closeTo(Point(6.0, 8.0, -10.0))))
    }
}