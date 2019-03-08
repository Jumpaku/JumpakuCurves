package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.times
import jumpaku.commons.json.parseJson
import jumpaku.commons.test.closeTo
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class VectorTest {

    val v = Vector(1.0, -2.0, 3.0)

    @Test
    fun testIJK() {
        println("IJK")
        assertThat(Vector.I, `is`(closeTo(Vector(1.0, 0.0, 0.0))))
        assertThat(Vector.J, `is`(closeTo(Vector(0.0, 1.0, 0.0))))
        assertThat(Vector.K, `is`(closeTo(Vector(0.0, 0.0, 1.0))))
    }

    @Test
    fun testPlus() {
        println("Plus")
        assertThat((v + Vector(-4.0, 5.0, -6.0)), `is`(closeTo(Vector(-3.0, 3.0, -3.0))))
    }

    @Test
    fun testMinus() {
        println("Minus")
        assertThat((v - Vector(-4.0, 5.0, -6.0)), `is`(closeTo(Vector(5.0, -7.0, 9.0))))
    }

    @Test
    fun testTimes() {
        println("Times")
        assertThat((v * 5.0), `is`(closeTo(Vector(5.0, -10.0, 15.0))))
        assertThat((5.0 * v), `is`(closeTo(Vector(5.0, -10.0, 15.0))))
    }

    @Test
    fun testDiv() {
        println("Div")
        assertThat((v / 5.0).orThrow(), `is`(closeTo(Vector(1 / 5.0, -2 / 5.0, 3 / 5.0))))
    }

    @Test
    fun testUnaryPlus() {
        println("UnaryPlus")
        assertThat((+v), `is`(closeTo(v)))
    }

    @Test
    fun testUnaryMinus() {
        println("UnaryMinus")
        assertThat((-v), `is`(closeTo(Vector(-1.0, 2.0, -3.0))))
    }

    @Test
    fun testNormalize() {
        println("Normalize")
        assertThat(v.normalize().orThrow(), `is`(closeTo(Vector(1.0 / FastMath.sqrt(14.0), -2.0 / FastMath.sqrt(14.0), 3.0 / FastMath.sqrt(14.0)))))
    }

    @Test
    fun testResize() {
        println("Resize")
        assertThat(v.resize(2.0).orThrow(), `is`(closeTo(Vector(1.0 / FastMath.sqrt(14.0) * 2, -2.0 / FastMath.sqrt(14.0) * 2, 3.0 / FastMath.sqrt(14.0) * 2))))
    }

    @Test
    fun testDot() {
        println("Dot")
        assertThat(v.dot(Vector(-4.0, 5.0, -6.0)), `is`(closeTo(-32.0)))
    }

    @Test
    fun testSquare() {
        println("Square")
        assertThat(v.square(), `is`(closeTo(14.0)))
    }

    @Test
    fun testLength() {
        println("Length")
        assertThat(v.length(), `is`(closeTo(FastMath.sqrt(14.0))))
    }

    @Test
    fun testCross() {
        println("Cross")
        assertThat(v.cross(Vector(-4.0, 5.0, -6.0)), `is`(closeTo(Vector(-3.0, -6.0, -3.0))))
    }

    @Test
    fun testAngle() {
        println("Angle")
        val v = Vector(1.0, 1.0, 1.0)
        val u = Vector(-1.0, -1.0, -1.0)
        val w = Vector(-1.0, -1.0, 2.0)
        assertThat(v.angle(v), `is`(closeTo(0.0)))
        assertThat(v.angle(u), `is`(closeTo(FastMath.PI)))
        assertThat(v.angle(w), `is`(closeTo(FastMath.PI / 2)))
    }

    @Test
    fun testJson() {
        println("Json")
        assertThat(v.toString().parseJson().tryMap { Vector.fromJson(it) }.orThrow(), `is`(closeTo(v)))
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = v.toDoubleArray()
        assertThat(a[0], `is`(closeTo(1.0)))
        assertThat(a[1], `is`(closeTo(-2.0)))
        assertThat(a[2], `is`(closeTo(3.0)))
    }
}