package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Translate
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class TranslateTest {

    val t = Translate(1.0, 2.0, -3.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(t(p), `is`(closeTo(Point(4.0, 6.0, -8.0))))
    }
}

