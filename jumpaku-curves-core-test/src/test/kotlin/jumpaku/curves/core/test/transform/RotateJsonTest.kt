package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.RotateJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import kotlin.math.sqrt

class RotateJsonTest {

    val t = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val p = Point(2.0, 2.0, 2.0)
    val r2 = sqrt(2.0)

    @Test
    fun testRotateJson() {
        println("RotateJson")
        val a = RotateJson.toJsonStr(t).parseJson().let { RotateJson.fromJson(it) }
        Assert.assertThat(a(p), Matchers.`is`(closeTo(Point(0.0, r2 * 2, 2.0))))
    }
}