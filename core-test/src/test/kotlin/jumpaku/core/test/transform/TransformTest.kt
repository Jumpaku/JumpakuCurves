package jumpaku.core.test.transform

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.json.parseJson
import jumpaku.core.test.geom.shouldEqualToPoint
import jumpaku.core.transform.*
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class TransformTest {

    val r2 = sqrt(2.0)

    val r = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val t = Translate(1.0, 2.0, -3.0)
    val s = UniformlyScale(2.0)

    val p = Point(2.0, 2.0, 2.0)
    val o = Point(0.0, 2.0, 2.0)

    @Test
    fun testAndThen() {
        println("AndThen")
        r.andThen(t).andThen(s)(p).shouldEqualToPoint(Point(2.0, r2*4+4, -2.0))
    }

    @Test
    fun testAt() {
        println("At")
        r.at(o)(p).shouldEqualToPoint(Point(r2, 2+r2, 2.0))
        t.at(o)(p).shouldEqualToPoint(Point(3.0, 4.0, -1.0))
        s.at(o)(p).shouldEqualToPoint(Point(4.0, 2.0, 2.0))
    }


    @Test
    fun testInvert() {
        println("Invert")
        r.invert().orThrow()(p).shouldEqualToPoint(Point(2*r2, 0.0, 2.0))
        t.invert().orThrow()(p).shouldEqualToPoint(Point(1.0, 0.0, 5.0))
        s.invert().orThrow()(p).shouldEqualToPoint(Point(1.0, 1.0, 1.0))
    }

    @Test
    fun testIdentity() {
        println("Identity")
        Transform.Identity(p).shouldEqualToPoint(p)
        Transform.Identity.invert().orThrow()(p).shouldEqualToPoint(p)
    }

    @Test
    fun testToMatrixJson() {
        println("ToMatrixJson")
        val e = r.at(o)(p)
        r.at(o).toMatrixJson()
                .toString().parseJson().tryFlatMap { Transform.fromMatrixJson(it) }.orThrow()(p).shouldEqualToPoint(e)
    }
}