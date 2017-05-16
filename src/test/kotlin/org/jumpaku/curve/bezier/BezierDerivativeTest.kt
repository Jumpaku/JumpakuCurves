package org.jumpaku.curve.bezier

import org.assertj.core.api.Assertions.*
import org.jumpaku.affine.*
import org.jumpaku.jsonAssertThat
import org.jumpaku.util.component1
import org.jumpaku.util.component2
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/16.
 */
class BezierDerivativeTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b4 = BezierDerivative(Vector(1.0, -2.0, 0.0), Vector(2.0,-1.0, 0.0), Vector(0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
        vectorAssertThat(b4.controlVectors[0]).isEqualToVector(Vector(1.0, -2.0, 0.0))
        vectorAssertThat(b4.controlVectors[1]).isEqualToVector(Vector(2.0,-1.0, 0.0))
        vectorAssertThat(b4.controlVectors[2]).isEqualToVector(Vector(0.0, 2.0))
        vectorAssertThat(b4.controlVectors[3]).isEqualToVector(Vector(2.0, 1.0, 0.0))
        vectorAssertThat(b4.controlVectors[4]).isEqualToVector(Vector(1.0, 2.0, 0.0))
        assertThat(b4.controlVectors.size()).isEqualTo(5)
        assertThat(b4.degree).isEqualTo(4)
        assertThat(b4.domain.begin).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b4.domain.end).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = BezierDerivative.fromJson(
                """{"controlVectors":[{"x":-2.0,"y":0.0,"z":0.0},{"x":-1.0,"y":0.0,"z":0.0},{"x":0.0,"y":2.0,"z":0.0},{"x":1.0,"y":0.0,"z":0.0},{"x":2.0,"y":0.0,"z":0.0}]}""")!!
        bezierAssertThat(p.asBezier).isEqualToBezier(
                Bezier(Crisp(-2.0, 0.0), Crisp(-1.0, 0.0), Crisp(0.0, 2.0), Crisp(1.0, 0.0), Crisp(2.0, 0.0)))
        jsonAssertThat(BezierDerivative.toJson(p)).isEqualToWithoutWhitespace(
                """{"controlVectors":[{"x":-2.0,"y":0.0,"z":0.0},{"x":-1.0,"y":0.0,"z":0.0},{"x":0.0,"y":2.0,"z":0.0},{"x":1.0,"y":0.0,"z":0.0},{"x":2.0,"y":0.0,"z":0.0}]}""")
        jsonAssertThat(p.toString()).isEqualToWithoutWhitespace(
                """{"controlVectors":[{"x":-2.0,"y":0.0,"z":0.0},{"x":-1.0,"y":0.0,"z":0.0},{"x":0.0,"y":2.0,"z":0.0},{"x":1.0,"y":0.0,"z":0.0},{"x":2.0,"y":0.0,"z":0.0}]}""")

        assertThat(BezierDerivative.fromJson("""{"controlVectors":{"r":2.0,"x":null,"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(BezierDerivative.fromJson("""{"controlVectors":{"r":2.0"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(BezierDerivative.fromJson("""{"controlVectors":"r":2.0,"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val b4 = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        vectorAssertThat(b4.evaluate(0.0 )).isEqualToVector(Vector(-2.0, 0.0      ))
        vectorAssertThat(b4.evaluate(0.25)).isEqualToVector(Vector(-1.0, 27 / 64.0))
        vectorAssertThat(b4.evaluate(0.5 )).isEqualToVector(Vector( 0.0, 0.75     ))
        vectorAssertThat(b4.evaluate(0.75)).isEqualToVector(Vector( 1.0, 27 / 64.0))
        vectorAssertThat(b4.evaluate(1.0 )).isEqualToVector(Vector( 2.0, 0.0      ))
    }

    @Test
    fun testDifferentiate() {
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        val d = b.derivative
        bezierAssertThat(d.asBezier).isEqualToBezier(Bezier(Crisp(4.0, 0.0), Crisp(4.0, 8.0), Crisp(4.0, -8.0), Crisp(4.0, 0.0)))
        vectorAssertThat(b.differentiate(0.0 )).isEqualToVector(Vector(4.0, 0.0 ))
        vectorAssertThat(b.differentiate(0.25)).isEqualToVector(Vector(4.0, 2.25))
        vectorAssertThat(b.differentiate(0.5 )).isEqualToVector(Vector(4.0, 0.0 ))
        vectorAssertThat(b.differentiate(0.75)).isEqualToVector(Vector(4.0,-2.25))
        vectorAssertThat(b.differentiate(1.0 )).isEqualToVector(Vector(4.0, 0.0 ))
        vectorAssertThat(d.evaluate(0.0 )).isEqualToVector(Vector(4.0, 0.0 ))
        vectorAssertThat(d.evaluate(0.25)).isEqualToVector(Vector(4.0, 2.25))
        vectorAssertThat(d.evaluate(0.5 )).isEqualToVector(Vector(4.0, 0.0 ))
        vectorAssertThat(d.evaluate(0.75)).isEqualToVector(Vector(4.0,-2.25))
        vectorAssertThat(d.evaluate(1.0 )).isEqualToVector(Vector(4.0, 0.0 ))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
                .restrict(0.25, 0.5)
        bezierAssertThat(b.asBezier).isEqualToBezier(Bezier(
                Crisp(-1.0, 27/64.0), Crisp(-3/4.0, 9/16.0), Crisp(-1/2.0, 11/16.0), Crisp(-1/4.0, 3/4.0), Crisp(0.0, 3/4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
                .reverse()
        bezierAssertThat(r.asBezier).isEqualToBezier(Bezier(
                Crisp(2.0, 0.0), Crisp(1.0, 0.0), Crisp(0.0, 2.0), Crisp(-1.0, 0.0), Crisp(-2.0, 0.0)))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val e = BezierDerivative(Vector(0.0, -1.0), Vector(2.0    , 0.0    ), Vector(0.0, 1.0)).elevate()
        val expected = Bezier(Crisp(0.0, -1.0), Crisp(4 / 3.0,-1 / 3.0), Crisp(4 / 3.0, 1 / 3.0), Crisp(0.0, 1.0))
        bezierAssertThat(e.asBezier).isEqualToBezier(expected)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = BezierDerivative(Vector(-1.0, -1.0), Vector(1.0, 1.0)).reduce()
        val e1 = Bezier(Crisp(0.0, 0.0))
        bezierAssertThat(b1.asBezier).isEqualToBezier(e1)

        val b2 = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 0.0), Vector(1.0, 0.0)).reduce()
        val e2 = Bezier(Crisp(-1.0, 0.0), Crisp(1.0, 0.0))
        bezierAssertThat(b2.asBezier).isEqualToBezier(e2)

        val b3 = BezierDerivative(Vector(-1.0, 0.0), Vector(-1 / 3.0, 4 / 3.0), Vector(1 / 3.0, 4 / 3.0), Vector(1.0, 0.0)).reduce()
        val e3 = Bezier(Crisp(-1.0, 0.0), Crisp(0.0, 2.0), Crisp(1.0, 0.0))
        bezierAssertThat(b3.asBezier).isEqualToBezier(e3)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (front, back) = BezierDerivative(Vector(1.0,-2.0, 0.0), Vector(2.0,-1.0, 0.0), Vector(0.0, 0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
                .subdivide(0.25)
        bezierAssertThat(front.asBezier).isEqualToBezier(Bezier(
                Crisp(1.0,      -2.0, 0.0),     Crisp(5/4.0,  -7/4.0, 0.0),     Crisp(21/16.0,-3/2.0, 1/8.0), Crisp(83/64.0,-5/4.0, 9/32.0), Crisp(322/256.0,-1.0, 27/64.0)))
        bezierAssertThat(back.asBezier ).isEqualToBezier(Bezier(
                Crisp(322/256.0,-1.0, 27/64.0), Crisp(73/64.0,-1/4.0, 27/32.0), Crisp(13/16.0, 1/2.0, 9/8.0), Crisp(7/4.0,   5/4.0, 0.0),    Crisp(1.0,       2.0, 0.0)))
    }
}