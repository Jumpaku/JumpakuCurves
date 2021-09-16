package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.AffineTransformJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class AffineAffineTransformJsonTest {

    val r = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))

    val p = Point(2.0, 2.0, 2.0)
    val o = Point(0.0, 2.0, 2.0)

    @Test
    fun testTransformJson() {
        println("TransformJson")
        val e = r.at(o)
        val a = AffineTransformJson.toJsonStr(r.at(o)).parseJson().let { AffineTransformJson.fromJson(it) }
        Assert.assertThat(a(p), Matchers.`is`(closeTo(e(p))))
    }
}