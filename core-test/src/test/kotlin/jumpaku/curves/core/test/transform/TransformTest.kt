package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.*
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
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
        assertThat(r.andThen(t).andThen(s)(p), `is`(closeTo(Point(2.0, r2*4+4, -2.0))))
    }

    @Test
    fun testAt() {
        println("At")
        assertThat(r.at(o)(p), `is`(closeTo(Point(r2, 2+r2, 2.0))))
        assertThat(t.at(o)(p), `is`(closeTo(Point(3.0, 4.0, -1.0))))
        assertThat(s.at(o)(p), `is`(closeTo(Point(4.0, 2.0, 2.0))))
    }


    @Test
    fun testInvert() {
        println("Invert")
        assertThat(r.invert().orThrow()(p), `is`(closeTo(Point(2*r2, 0.0, 2.0))))
        assertThat(t.invert().orThrow()(p), `is`(closeTo(Point(1.0, 0.0, 5.0))))
        assertThat(s.invert().orThrow()(p), `is`(closeTo(Point(1.0, 1.0, 1.0))))
    }

    @Test
    fun testIdentity() {
        println("Identity")
        assertThat(Transform.Identity(p), `is`(closeTo(p)))
        assertThat(Transform.Identity.invert().orThrow()(p), `is`(closeTo(p)))
    }

    @Test
    fun testToMatrixJson() {
        println("ToMatrixJson")
        val e = r.at(o)(p)
        assertThat(r.at(o).toMatrixJson()
                .toString().parseJson().tryMap { Transform.fromMatrixJson(it) }.orThrow()(p), `is`(closeTo(e)))
    }
}