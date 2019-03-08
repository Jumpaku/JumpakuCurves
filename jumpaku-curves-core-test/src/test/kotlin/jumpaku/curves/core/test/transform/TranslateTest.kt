package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Translate
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test

class TranslateTest {

    val t = Translate(1.0, 2.0, -3.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(t(p), `is`(closeTo(Point(4.0, 6.0, -8.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().tryMap { Translate.fromJson(it) }.orThrow()(p)
        assertThat(a, `is`(closeTo(Point(4.0, 6.0, -8.0))))
    }
}