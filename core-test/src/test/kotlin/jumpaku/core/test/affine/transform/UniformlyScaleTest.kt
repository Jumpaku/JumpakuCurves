package jumpaku.core.test.affine.transform

import jumpaku.core.affine.Point
import jumpaku.core.affine.transform.Translate
import jumpaku.core.affine.transform.UniformlyScale
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToPoint
import org.junit.jupiter.api.Test

class UniformlyScaleTest {

    val t = UniformlyScale(2.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        t(p).shouldEqualToPoint(Point(6.0, 8.0, -10.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().flatMap { UniformlyScale.fromJson(it) }.get()
        a(p).shouldEqualToPoint(Point(6.0, 8.0, -10.0))
    }
}