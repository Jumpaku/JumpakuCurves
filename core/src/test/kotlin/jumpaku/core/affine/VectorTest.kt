package jumpaku.core.affine

import jumpaku.core.json.parseJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.junit.Test

fun vectorAssertThat(actual: Vector): VectorAssert = VectorAssert(actual)

class VectorAssert(actual: Vector) : AbstractAssert<VectorAssert, Vector>(actual, VectorAssert::class.java) {

    fun isEqualToVector(expected: Vector, eps: Double = 1.0e-10): VectorAssert {
        isNotNull

        Assertions.assertThat(actual.x).`as`("x of point").isEqualTo(expected.x, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.y).`as`("y of point").isEqualTo(expected.y, Assertions.withPrecision(eps))
        Assertions.assertThat(actual.z).`as`("z of point").isEqualTo(expected.z, Assertions.withPrecision(eps))

        return this
    }
}

class VectorTest {

    @Test
    fun testProperties() {
        println("Properties")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(v.y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(v.z).isEqualTo( 3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testIJK() {
        println("IJK")
        vectorAssertThat(Vector.I).isEqualToVector( Vector(1.0, 0.0,0.0))
        vectorAssertThat(Vector.J).isEqualToVector( Vector(0.0,1.0, 0.0))
        vectorAssertThat(Vector.K).isEqualToVector( Vector(0.0, 0.0,1.0))
    }

    @Test
    fun testPlus() {
        println("Plus")
        val v = Vector(1.0, -2.0, 3.0).plus(Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(v).isEqualToVector(Vector(-3.0, 3.0, -3.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val v = Vector(1.0, -2.0, 3.0).minus(Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(v).isEqualToVector(Vector(5.0, -7.0, 9.0))
    }

    @Test
    fun testTimes() {
        println("Times")
        val v = Vector(1.0, -2.0, 3.0).times(5.0)
        val u = 5.0.times(Vector(1.0, -2.0, 3.0))
        vectorAssertThat(v).isEqualToVector(Vector(5.0, -10.0, 15.0))
        vectorAssertThat(u).isEqualToVector(Vector(5.0, -10.0, 15.0))
    }

    @Test
    fun testDiv() {
        println("Div")
        val v0 = Vector(1.0, -2.0, 3.0).div(5.0)
        vectorAssertThat(v0).isEqualToVector(Vector(1 / 5.0, -2 / 5.0, 3 / 5.0))
        val v1 = Vector(1.0, -2.0, 3.0).divOption(5.0)
        vectorAssertThat(v1.get()).isEqualToVector(Vector(1 / 5.0, -2 / 5.0, 3 / 5.0))
    }

    @Test
    fun testUnaryPlus() {
        println("UnaryPlus")
        val v = Vector(1.0, -2.0, 3.0).unaryPlus()
        vectorAssertThat(v).isEqualToVector(Vector(1.0, -2.0, 3.0))
    }

    @Test
    fun testUnaryMinus() {
        println("UnaryMinus")
        val v = Vector(1.0, -2.0, 3.0).unaryMinus()
        vectorAssertThat(v).isEqualToVector(Vector(-1.0, 2.0, -3.0))
    }

    @Test
    fun testNormalize() {
        println("Normalize")
        val v = Vector(1.0, -2.0, 3.0).normalize()
        vectorAssertThat(v.get()).isEqualToVector(Vector(1.0 / FastMath.sqrt(14.0), -2.0 / FastMath.sqrt(14.0), 3.0 / FastMath.sqrt(14.0)))
    }

    @Test
    fun testResize() {
        println("Resize")
        val v = Vector(1.0, -2.0, 3.0).resize(2.0)
        vectorAssertThat(v.get()).isEqualToVector(Vector(1.0 / FastMath.sqrt(14.0) * 2, -2.0 / FastMath.sqrt(14.0) * 2, 3.0 / FastMath.sqrt(14.0) * 2))
    }

    @Test
    fun testDot() {
        println("Dot")
        val v = Vector(1.0, -2.0, 3.0)
        val u = Vector(-4.0, 5.0, -6.0)
        assertThat(v.dot(u)).isEqualTo(-32.0, withPrecision(1.0e-10))
    }

    @Test
    fun testSquare() {
        println("Square")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.square()).isEqualTo(14.0, withPrecision(1.0e-10))
    }

    @Test
    fun testLength() {
        println("Length")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.length()).isEqualTo(FastMath.sqrt(14.0), withPrecision(1.0e-10))
    }

    @Test
    fun testCross() {
        println("Cross")
        val v = Vector(1.0, -2.0, 3.0)
        val u = Vector(-4.0, 5.0, -6.0)
        vectorAssertThat(v.cross(u)).isEqualToVector(Vector(-3.0, -6.0, -3.0))
    }

    @Test
    fun testAngle() {
        println("Angle")
        val v = Vector(1.0, 1.0, 1.0)
        val u = Vector(-1.0, -1.0, -1.0)
        val w = Vector(-1.0, -1.0, 2.0)
        assertThat(v.angle(v)).isEqualTo(          0.0, withPrecision(1.0e-10))
        assertThat(v.angle(u)).isEqualTo(  FastMath.PI, withPrecision(1.0e-10))
        assertThat(v.angle(w)).isEqualTo(FastMath.PI/2, withPrecision(1.0e-10))
    }

    @Test
    fun testJson() {
        println("Json")
        val v = Vector(1.0, -2.0, 3.0)

        vectorAssertThat(v.toString().parseJson().flatMap { Vector.fromJson(it) }.get()).isEqualToVector(v)
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = Vector(1.0, -2.0, 3.0).toArray()
        assertThat(a[0]).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(a[1]).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(a[2]).isEqualTo( 3.0, withPrecision(1.0e-10))
    }
}