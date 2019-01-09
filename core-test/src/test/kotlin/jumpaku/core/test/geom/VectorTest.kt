package jumpaku.core.test.geom

import jumpaku.core.geom.Vector
import jumpaku.core.geom.times
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class VectorTest {

    val v = Vector(1.0, -2.0, 3.0)

    @Test
    fun testIJK() {
        println("IJK")
        Vector.I.shouldEqualToVector(Vector(1.0, 0.0, 0.0))
        Vector.J.shouldEqualToVector(Vector(0.0, 1.0, 0.0))
        Vector.K.shouldEqualToVector(Vector(0.0, 0.0, 1.0))
    }

    @Test
    fun testPlus() {
        println("Plus")
        (v + Vector(-4.0, 5.0, -6.0)).shouldEqualToVector(Vector(-3.0, 3.0, -3.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        (v - Vector(-4.0, 5.0, -6.0)).shouldEqualToVector(Vector(5.0, -7.0, 9.0))
    }

    @Test
    fun testTimes() {
        println("Times")
        (v * 5.0).shouldEqualToVector(Vector(5.0, -10.0, 15.0))
        (5.0 * v).shouldEqualToVector(Vector(5.0, -10.0, 15.0))
    }

    @Test
    fun testDiv() {
        println("Div")
        (v / 5.0).orThrow().shouldEqualToVector(Vector(1 / 5.0, -2 / 5.0, 3 / 5.0))
    }

    @Test
    fun testUnaryPlus() {
        println("UnaryPlus")
        (+v).shouldEqualToVector(v)
    }

    @Test
    fun testUnaryMinus() {
        println("UnaryMinus")
        (-v).shouldEqualToVector(Vector(-1.0, 2.0, -3.0))
    }

    @Test
    fun testNormalize() {
        println("Normalize")
        v.normalize().orThrow().shouldEqualToVector(Vector(1.0 / FastMath.sqrt(14.0), -2.0 / FastMath.sqrt(14.0), 3.0 / FastMath.sqrt(14.0)))
    }

    @Test
    fun testResize() {
        println("Resize")
        v.resize(2.0).orThrow().shouldEqualToVector(Vector(1.0 / FastMath.sqrt(14.0) * 2, -2.0 / FastMath.sqrt(14.0) * 2, 3.0 / FastMath.sqrt(14.0) * 2))
    }

    @Test
    fun testDot() {
        println("Dot")
        v.dot(Vector(-4.0, 5.0, -6.0)).shouldBeCloseTo(-32.0)
    }

    @Test
    fun testSquare() {
        println("Square")
        v.square().shouldBeCloseTo(14.0)
    }

    @Test
    fun testLength() {
        println("Length")
        v.length().shouldBeCloseTo(FastMath.sqrt(14.0))
    }

    @Test
    fun testCross() {
        println("Cross")
        v.cross(Vector(-4.0, 5.0, -6.0)).shouldEqualToVector(Vector(-3.0, -6.0, -3.0))
    }

    @Test
    fun testAngle() {
        println("Angle")
        val v = Vector(1.0, 1.0, 1.0)
        val u = Vector(-1.0, -1.0, -1.0)
        val w = Vector(-1.0, -1.0, 2.0)
        v.angle(v).shouldBeCloseTo(0.0)
        v.angle(u).shouldBeCloseTo(FastMath.PI)
        v.angle(w).shouldBeCloseTo(FastMath.PI / 2)
    }

    @Test
    fun testJson() {
        println("Json")
        v.toString().parseJson().tryFlatMap { Vector.fromJson(it) }.orThrow().shouldEqualToVector(v)
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = v.toDoubleArray()
        a[0].shouldBeCloseTo(1.0)
        a[1].shouldBeCloseTo(-2.0)
        a[2].shouldBeCloseTo(3.0)
    }
}