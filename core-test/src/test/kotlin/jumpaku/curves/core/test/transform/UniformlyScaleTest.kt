package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test

class UniformlyScaleTest {

    val t = UniformlyScale(2.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(t(p), `is`(closeTo(Point(6.0, 8.0, -10.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().tryMap { UniformlyScale.fromJson(it) }.orThrow()
        assertThat(a(p), `is`(closeTo(Point(6.0, 8.0, -10.0))))
    }
}