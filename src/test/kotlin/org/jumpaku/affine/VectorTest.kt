package org.jumpaku.affine

import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
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
        val v = Vector( 1.0,-2.0, 3.0)
        val u = Vector(-4.0, 5.0, -6.0)
        assertThat(v.plus(u).x).isEqualTo(-3.0, withPrecision(1.0e-10))
        assertThat(v.plus(u).y).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat(v.plus(u).z).isEqualTo(-3.0, withPrecision(1.0e-10))
        assertThat((v+u).x).isEqualTo(-3.0, withPrecision(1.0e-10))
        assertThat((v+u).y).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat((v+u).z).isEqualTo(-3.0, withPrecision(1.0e-10))
        val a = 2.0
        assertThat(v.plus(a, u).x).isEqualTo(-7.0, withPrecision(1.0e-10))
        assertThat(v.plus(a, u).y).isEqualTo( 8.0, withPrecision(1.0e-10))
        assertThat(v.plus(a, u).z).isEqualTo(-9.0, withPrecision(1.0e-10))
    }

    @Test
    fun testMinus() {
        println("Minus")
        val v = Vector( 1.0,-2.0, 3.0)
        val u = Vector(-4.0, 5.0, -6.0)
        assertThat(v.minus(u).x).isEqualTo( 5.0, withPrecision(1.0e-10))
        assertThat(v.minus(u).y).isEqualTo(-7.0, withPrecision(1.0e-10))
        assertThat(v.minus(u).z).isEqualTo( 9.0, withPrecision(1.0e-10))
        assertThat((v-u).x).isEqualTo( 5.0, withPrecision(1.0e-10))
        assertThat((v-u).y).isEqualTo(-7.0, withPrecision(1.0e-10))
        assertThat((v-u).z).isEqualTo( 9.0, withPrecision(1.0e-10))
        val a = 2.0
        assertThat(v.minus(a, u).x).isEqualTo(  9.0, withPrecision(1.0e-10))
        assertThat(v.minus(a, u).y).isEqualTo(-12.0, withPrecision(1.0e-10))
        assertThat(v.minus(a, u).z).isEqualTo( 15.0, withPrecision(1.0e-10))
    }

    @Test
    fun testTimes() {
        println("Times")
        val v = Vector(1.0, -2.0, 3.0)
        val s = 5.0
        assertThat(v.times(s).x).isEqualTo(  5.0, withPrecision(1.0e-10))
        assertThat(v.times(s).y).isEqualTo(-10.0, withPrecision(1.0e-10))
        assertThat(v.times(s).z).isEqualTo( 15.0, withPrecision(1.0e-10))
        assertThat((v*s).x).isEqualTo(  5.0, withPrecision(1.0e-10))
        assertThat((v*s).y).isEqualTo(-10.0, withPrecision(1.0e-10))
        assertThat((v*s).z).isEqualTo( 15.0, withPrecision(1.0e-10))
        assertThat(s.times(v).x).isEqualTo(  5.0, withPrecision(1.0e-10))
        assertThat(s.times(v).y).isEqualTo(-10.0, withPrecision(1.0e-10))
        assertThat(s.times(v).z).isEqualTo( 15.0, withPrecision(1.0e-10))
        assertThat((s*v).x).isEqualTo(  5.0, withPrecision(1.0e-10))
        assertThat((s*v).y).isEqualTo(-10.0, withPrecision(1.0e-10))
        assertThat((s*v).z).isEqualTo( 15.0, withPrecision(1.0e-10))
    }

    @Test
    fun testUnaryPlus() {
        println("UnaryPlus")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.unaryPlus().x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(v.unaryPlus().y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(v.unaryPlus().z).isEqualTo( 3.0, withPrecision(1.0e-10))
        assertThat((+v).x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat((+v).y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat((+v).z).isEqualTo( 3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testUnaryMinus() {
        println("UnaryMinus")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.unaryMinus().x).isEqualTo(-1.0, withPrecision(1.0e-10))
        assertThat(v.unaryMinus().y).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(v.unaryMinus().z).isEqualTo(-3.0, withPrecision(1.0e-10))
        assertThat((-v).x).isEqualTo(-1.0, withPrecision(1.0e-10))
        assertThat((-v).y).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat((-v).z).isEqualTo(-3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testNegate() {
        println("Negate")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.negate().x).isEqualTo(-1.0, withPrecision(1.0e-10))
        assertThat(v.negate().y).isEqualTo( 2.0, withPrecision(1.0e-10))
        assertThat(v.negate().z).isEqualTo(-3.0, withPrecision(1.0e-10))
    }

    @Test
    fun testNormalize() {
        println("Normalize")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.normalize().x).isEqualTo( 1.0/FastMath.sqrt(14.0), withPrecision(1.0e-10))
        assertThat(v.normalize().y).isEqualTo(-2.0/FastMath.sqrt(14.0), withPrecision(1.0e-10))
        assertThat(v.normalize().z).isEqualTo( 3.0/FastMath.sqrt(14.0), withPrecision(1.0e-10))
    }

    @Test
    fun testResize() {
        println("Resize")
        val v = Vector(1.0, -2.0, 3.0)
        assertThat(v.resize(2.0).x).isEqualTo( 1.0/FastMath.sqrt(14.0)*2, withPrecision(1.0e-10))
        assertThat(v.resize(2.0).y).isEqualTo(-2.0/FastMath.sqrt(14.0)*2, withPrecision(1.0e-10))
        assertThat(v.resize(2.0).z).isEqualTo( 3.0/FastMath.sqrt(14.0)*2, withPrecision(1.0e-10))
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
        assertThat(v.cross(u).x).isEqualTo(-3.0, withPrecision(1.0e-10))
        assertThat(v.cross(u).y).isEqualTo(-6.0, withPrecision(1.0e-10))
        assertThat(v.cross(u).z).isEqualTo(-3.0, withPrecision(1.0e-10))
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
    fun testToString() {
        println("ToString")
        val v = Vector(1.0, -2.0, 3.0)

        assertThat(Vector.fromJson("""{"x":1.0, "y":-2.0, "z":3.0}""")!!.x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson("""{"x":1.0, "y":-2.0, "z":3.0}""")!!.y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson("""{"x":1.0, "y":-2.0, "z":3.0}""")!!.z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Vector.fromJson(Vector.toJson(v))!!.x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson(Vector.toJson(v))!!.y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson(Vector.toJson(v))!!.z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Vector.fromJson(v.toString())!!.x).isEqualTo( 1.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson(v.toString())!!.y).isEqualTo(-2.0, withPrecision(1.0e-10))
        assertThat(Vector.fromJson(v.toString())!!.z).isEqualTo( 3.0, withPrecision(1.0e-10))

        assertThat(Vector.fromJson("""{"x":null, "y"-2.0, "z":3.0}""")).isNull()
        assertThat(Vector.fromJson("""{"x":1.0"y"-2.0, "z":3.0}""")).isNull()
        assertThat(Vector.fromJson(""""x":1.0, "y":-2.0, "z":3.0}""")).isNull()
    }
}