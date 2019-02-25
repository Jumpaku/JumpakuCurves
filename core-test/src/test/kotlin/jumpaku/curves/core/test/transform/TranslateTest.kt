package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import org.junit.jupiter.api.Test

class TranslateTest {

    val t = Translate(1.0, 2.0, -3.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        t(p).shouldEqualToPoint(Point(4.0, 6.0, -8.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().tryMap { Translate.fromJson(it) }.orThrow()(p)
        a.shouldEqualToPoint(Point(4.0, 6.0, -8.0))
    }
}