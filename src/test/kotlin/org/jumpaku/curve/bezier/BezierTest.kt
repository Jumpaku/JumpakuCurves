package org.jumpaku.curve.bezier

import org.assertj.core.api.Assertions.*
import org.jumpaku.util.*
import org.jumpaku.affine.*
import org.jumpaku.jsonAssertThat
import org.junit.Test

class BezierTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b4 = Bezier(Fuzzy(1.0, -2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
        pointAssertThat(b4.controlPoints[0]).isEqualToPoint(Fuzzy(1.0, -2.0, 0.0))
        pointAssertThat(b4.controlPoints[1]).isEqualToPoint(Fuzzy(2.0,-1.0, 0.0))
        pointAssertThat(b4.controlPoints[2]).isEqualToPoint(Crisp(0.0, 2.0))
        pointAssertThat(b4.controlPoints[3]).isEqualToPoint(Fuzzy(2.0, 1.0, 0.0))
        pointAssertThat(b4.controlPoints[4]).isEqualToPoint(Fuzzy(1.0, 2.0, 0.0))
        assertThat(b4.controlPoints.size()).isEqualTo(5)
        assertThat(b4.degree).isEqualTo(4)
        assertThat(b4.domain.begin).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b4.domain.end).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = Bezier.fromJson(
                """{"controlPoints":[{"r":1.0,"x":-2.0,"y":0.0,"z":0.0},{"r":2.0,"x":-1.0,"y":0.0,"z":0.0},{"r":0.0,"x":0.0,"y":2.0,"z":0.0},{"r":2.0,"x":1.0,"y":0.0,"z":0.0},{"r":1.0,"x":2.0,"y":0.0,"z":0.0}]}""")!!
        bezierAssertThat(p).isEqualToBezier(
                Bezier(Fuzzy(1.0, -2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0)))
        jsonAssertThat(Bezier.toJson(p)).isEqualToWithoutWhitespace(
                """{"controlPoints":[{"r":1.0,"x":-2.0,"y":0.0,"z":0.0},{"r":2.0,"x":-1.0,"y":0.0,"z":0.0},{"r":0.0,"x":0.0,"y":2.0,"z":0.0},{"r":2.0,"x":1.0,"y":0.0,"z":0.0},{"r":1.0,"x":2.0,"y":0.0,"z":0.0}]}""")
        jsonAssertThat(p.toString()).isEqualToWithoutWhitespace(
                """{"controlPoints":[{"r":1.0,"x":-2.0,"y":0.0,"z":0.0},{"r":2.0,"x":-1.0,"y":0.0,"z":0.0},{"r":0.0,"x":0.0,"y":2.0,"z":0.0},{"r":2.0,"x":1.0,"y":0.0,"z":0.0},{"r":1.0,"x":2.0,"y":0.0,"z":0.0}]}""")

        assertThat(Bezier.fromJson("""{"controlPoints":{"r":2.0,"x":null,"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(Bezier.fromJson("""{"controlPoints":{"r":2.0"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(Bezier.fromJson("""{"controlPoints":"r":2.0,"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val b4 = Bezier(Fuzzy(1.0,-2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
        pointAssertThat(b4.evaluate(0.0 )).isEqualToPoint(Fuzzy(1.0        , -2.0, 0.0      ))
        pointAssertThat(b4.evaluate(0.25)).isEqualToPoint(Fuzzy(161.0 / 128, -1.0, 27 / 64.0))
        pointAssertThat(b4.evaluate(0.5 )).isEqualToPoint(Fuzzy(9.0 / 8    ,  0.0, 0.75     ))
        pointAssertThat(b4.evaluate(0.75)).isEqualToPoint(Fuzzy(161.0 / 128,  1.0, 27 / 64.0))
        pointAssertThat(b4.evaluate(1.0 )).isEqualToPoint(Fuzzy(1.0        ,  2.0, 0.0      ))
    }

    @Test
    fun testDifferentiate() {
        val b = Bezier(Fuzzy(1.0,-2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
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
        val b = Bezier(Fuzzy(1.0,-2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
                .restrict(0.25, 0.5)
        bezierAssertThat(b).isEqualToBezier(Bezier(
                Fuzzy(161/128.0,-1.0, 27/64.0), Fuzzy(39/32.0,-3/4.0, 9/16.0), Fuzzy(37/32.0,-1/2.0, 11/16.0), Fuzzy(9/8.0,-1/4.0, 3/4.0), Fuzzy(9/8.0, 0.0, 3/4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = Bezier(Fuzzy(1.0,-2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
                .reverse()
        bezierAssertThat(r).isEqualToBezier(Bezier(
                Fuzzy(1.0, 2.0, 0.0), Fuzzy(2.0, 1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0,-1.0, 0.0), Fuzzy(1.0,-2.0, 0.0)))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val instance = Bezier(Fuzzy(0.0, -1.0), Fuzzy(2.0    , 0.0    ), Fuzzy(0.0, 1.0))
        val expected = Bezier(Fuzzy(0.0, -1.0), Fuzzy(4 / 3.0,-1 / 3.0), Fuzzy(4 / 3.0, 1 / 3.0), Fuzzy(0.0, 1.0))
        bezierAssertThat(instance.elevate()).isEqualToBezier(expected)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = Bezier(Fuzzy(-1.0, -1.0), Fuzzy(1.0, 1.0)).reduce()
        val e1 = Bezier(Fuzzy(0.0, 0.0))
        bezierAssertThat(b1).isEqualToBezier(e1)

        val b2 = Bezier(Fuzzy(-1.0, 0.0), Fuzzy(0.0, 0.0), Fuzzy(1.0, 0.0)).reduce()
        val e2 = Bezier(Fuzzy(-1.0, 0.0), Fuzzy(1.0, 0.0))
        bezierAssertThat(b2).isEqualToBezier(e2)

        val b3 = Bezier(Fuzzy(-1.0, 0.0), Fuzzy(-1 / 3.0, 4 / 3.0), Fuzzy(1 / 3.0, 4 / 3.0), Fuzzy(1.0, 0.0)).reduce()
        val e3 = Bezier(Fuzzy(-1.0, 0.0), Fuzzy(0.0, 2.0), Fuzzy(1.0, 0.0))
        bezierAssertThat(b3).isEqualToBezier(e3)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (front, back) = Bezier(Fuzzy(1.0,-2.0, 0.0), Fuzzy(2.0,-1.0, 0.0), Crisp(0.0, 2.0), Fuzzy(2.0, 1.0, 0.0), Fuzzy(1.0, 2.0, 0.0))
                .subdivide(0.25)
        bezierAssertThat(front).isEqualToBezier(Bezier(
                Fuzzy(1.0,      -2.0, 0.0),     Fuzzy(5/4.0,  -7/4.0, 0.0),     Fuzzy(21/16.0, -3/2.0, 1/8.0), Fuzzy(83/64.0,-5/4.0, 9/32.0), Fuzzy(322/256.0,-1.0, 27/64.0)))
        bezierAssertThat(back ).isEqualToBezier(Bezier(
                Fuzzy(322/256.0,-1.0, 27/64.0), Fuzzy(73/64.0,-1/4.0, 27/32.0), Fuzzy(13/16.0, 1/2.0, 9/8.0), Fuzzy(7/4.0,   5/4.0, 0.0),    Fuzzy(1.0,       2.0, 0.0)))
    }
}