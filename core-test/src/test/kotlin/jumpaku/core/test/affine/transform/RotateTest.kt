package jumpaku.core.test.affine.transform

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.transform.Rotate
import jumpaku.core.affine.transform.UniformlyScale
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToPoint
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
        val a = t.toString().parseJson().flatMap { Rotate.fromJson(it) }.get()
        a(p).shouldEqualToPoint(Point(0.0, r2*2, 2.0))
    }
}