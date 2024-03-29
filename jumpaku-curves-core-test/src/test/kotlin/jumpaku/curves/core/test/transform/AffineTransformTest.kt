package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.*
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class AffineTransformTest {

    val r2 = sqrt(2.0)

    val r = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val t = Translate(1.0, 2.0, -3.0)
    val s = UniformlyScale(2.0)

    val p = Point(2.0, 2.0, 2.0)
    val o = Point(0.0, 2.0, 2.0)

    @Test
    fun testAndThen() {
        println("AndThen")
        assertThat(r.andThen(t).andThen(s)(p), `is`(closeTo(Point(2.0, r2 * 4 + 4, -2.0))))
    }

    @Test
    fun testAt() {
        println("At")
        assertThat(r.at(o)(p), `is`(closeTo(Point(r2, 2 + r2, 2.0))))
        assertThat(t.at(o)(p), `is`(closeTo(Point(3.0, 4.0, -1.0))))
        assertThat(s.at(o)(p), `is`(closeTo(Point(4.0, 2.0, 2.0))))
    }


    @Test
    fun testInvert() {
        println("Invert")
        assertThat(r.invert().orThrow()(p), `is`(closeTo(Point(2 * r2, 0.0, 2.0))))
        assertThat(t.invert().orThrow()(p), `is`(closeTo(Point(1.0, 0.0, 5.0))))
        assertThat(s.invert().orThrow()(p), `is`(closeTo(Point(1.0, 1.0, 1.0))))
    }

    @Test
    fun testIdentity() {
        println("Identity")
        assertThat(AffineTransform.Identity(p), `is`(closeTo(p)))
        assertThat(AffineTransform.Identity.invert().orThrow()(p), `is`(closeTo(p)))
    }

    @Test
    fun testCalibrateByFitting() {
        println("CalibrateByFitting")
        val p0 = Point(1.0, -2.0, 3.0) to Point(-4.0, 5.0, -6.0)
        val p1 = Point(-1.0, 2.0, -3.0) to Point(4.0, 5.0, 6.0)
        val p2 = Point(-1.0, -2.0, -3.0) to Point(3.0, 2.0, 1.0)
        val p3 = Point(1.0, 2.0, 33.0) to Point(-3.0, -2.0, -1.0)
        val before = listOf(
            Point.xyz(1.0, 2.0, 3.0),
            Point.xyz(2.0, 3.0, 4.0),
            Point.xyz(3.0, 4.0, 5.0),
            Point.xyz(4.0, 5.0, 6.0),
            Point.xyz(5.0, 6.0, 7.0),
            Point.xyz(7.0, 8.0, 9.0),
        )
        val after = before.map(Calibrate(p0, p1, p2, p3))
        val transform = AffineTransform.calibrateByFitting(before.zip(after))
        for (i in before.indices){
            val a = transform(before[i])
            val e = after[i]
            assertThat(a, `is`(closeTo(e)))
        }
    }
}

