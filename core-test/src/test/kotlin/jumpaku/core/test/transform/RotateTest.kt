package jumpaku.core.test.transform

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.json.parseJson
import jumpaku.core.test.geom.shouldEqualToPoint
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class RotateTest {

    val t = Rotate(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val p = Point(2.0, 2.0, 2.0)
    val r2 = sqrt(2.0)

    @Test
    fun testInvoke() {
        println("Invoke")
        t(p).shouldEqualToPoint(Point(0.0, r2*2, 2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = t.toString().parseJson().tryFlatMap { Rotate.fromJson(it) }.orThrow()
        a(p).shouldEqualToPoint(Point(0.0, r2*2, 2.0))
    }
}