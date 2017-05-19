package org.jumpaku.affine

import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.jumpaku.jsonAssertThat
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/09.
 */
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
    fun testPlus() {
        println("Plus")
        val v = Vector( 1.0,-2.0, 3.0).plus(Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(v).isEqualToVector(Vector(-3.0, 3.0, -3.0))
        val u = Vector( 1.0,-2.0, 3.0).plus(2.0, Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(u).isEqualToVector(Vector(-7.0, 8.0, -9.0))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val v = Vector( 1.0,-2.0, 3.0).minus(Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(v).isEqualToVector(Vector( 5.0,-7.0,  9.0))
        val u = Vector( 1.0,-2.0, 3.0).minus(2.0, Vector(-4.0, 5.0, -6.0))
        vectorAssertThat(u).isEqualToVector(Vector( 9.0,-12.0, 15.0))
    }

    @Test
    fun testTimes() {
        println("Times")
        val v = Vector(1.0, -2.0, 3.0).times(5.0)
        val u = 5.0.times(Vector(1.0, -2.0, 3.0))
        vectorAssertThat(v).isEqualToVector(Vector( 5.0,-10.0, 15.0))
        vectorAssertThat(u).isEqualToVector(Vector( 5.0,-10.0, 15.0))
    }

    @Test
    fun testUnaryPlus() {
        println("UnaryPlus")
        val v = Vector(1.0, -2.0, 3.0).unaryPlus()
        vectorAssertThat(v).isEqualToVector(Vector( 1.0, -2.0,  3.0))
    }

    @Test
    fun testUnaryMinus() {
        println("UnaryMinus")
        val v = Vector(1.0, -2.0, 3.0).unaryMinus()
        vectorAssertThat(v).isEqualToVector(Vector(-1.0,  2.0, -3.0))
    }

    @Test
    fun testNormalize() {
        println("Normalize")
        val v = Vector(1.0, -2.0, 3.0).normalize()
        vectorAssertThat(v).isEqualToVector(Vector( 1.0/FastMath.sqrt(14.0), -2.0/FastMath.sqrt(14.0),  3.0/FastMath.sqrt(14.0)))
    }

    @Test
    fun testResize() {
        println("Resize")
        val v = Vector(1.0, -2.0, 3.0).resize(2.0)
        vectorAssertThat(v).isEqualToVector(Vector( 1.0/FastMath.sqrt(14.0)*2,-2.0/FastMath.sqrt(14.0)*2, 3.0/FastMath.sqrt(14.0)*2))
    }

    @Test
    fun testDot() {
        println("Dot")
        val v = Vector( 1.0, -2.0, 3.0)
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
        val v = Vector( 1.0,-2.0, 3.0)
        val u = Vector(-4.0, 5.0, -6.0)
        vectorAssertThat(v.cross(u)).isEqualToVector(Vector(-3.0,-6.0, -3.0))
    }

    @Test
    fun testAngle() {
        println("Angle")
        val v = Vector( 1.0, 1.0, 1.0)
        val u = Vector(-1.0,-1.0,-1.0)
        val w = Vector(-1.0,-1.0, 2.0)
        assertThat(v.angle(v)).isEqualTo(          0.0, withPrecision(1.0e-10))
        assertThat(v.angle(u)).isEqualTo(  FastMath.PI, withPrecision(1.0e-10))
        assertThat(v.angle(w)).isEqualTo(FastMath.PI/2, withPrecision(1.0e-10))
    }

    @Test
    fun testJson() {
        println("Json")
        val v = Vector(1.0, -2.0, 3.0)

        vectorAssertThat(VectorJson.fromJson(VectorJson.toJson(v)).get()).isEqualToVector(v)
        vectorAssertThat(VectorJson.fromJson(v.toString()).get()).isEqualToVector(v)

        assertThat(VectorJson.fromJson("""{"x":null, "y"-2.0, "z":3.0}""").isEmpty).isTrue()
        assertThat(VectorJson.fromJson("""{"x":1.0"y"-2.0, "z":3.0}""").isEmpty).isTrue()
        assertThat(VectorJson.fromJson(""""x":1.0, "y":-2.0, "z":3.0}""").isEmpty).isTrue()
    }
}