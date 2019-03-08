package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class RotateTest {

    val t = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val p = Point(2.0, 2.0, 2.0)
    val r2 = sqrt(2.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(t(p), `is`(closeTo(Point(0.0, r2*2, 2.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().tryMap { Rotate.fromJson(it) }.orThrow()
        assertThat(a(p), `is`(closeTo(Point(0.0, r2*2, 2.0))))
    }
}