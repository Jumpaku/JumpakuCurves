package org.jumpaku.curve.bezier

import org.assertj.core.api.Assertions.*
import org.jumpaku.affine.*
import org.jumpaku.curve.Interval
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
        val p = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        bezierAssertThat(BezierDerivativeJson.fromJson(p.toString()).get().asBezier).isEqualToBezier(p.asBezier)
        bezierAssertThat(BezierDerivativeJson.fromJson(BezierDerivativeJson.toJson(p)).get().asBezier).isEqualToBezier(p.asBezier)

        assertThat(BezierDerivativeJson.fromJson("""{"controlVectors":{"r":2.0,"x":null,"y":1.0,"z":0.0}]}""").isEmpty).isTrue()
        assertThat(BezierDerivativeJson.fromJson("""{"controlVectors":{"r":2.0"x":-1.0"y":1.0,"z":0.0}]}""").isEmpty).isTrue()
        assertThat(BezierDerivativeJson.fromJson("""{"controlVectors":"r":2.0,"x":-1.0"y":1.0,"z":0.0}]}""").isEmpty).isTrue()
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
        bezierAssertThat(d.asBezier).isEqualToBezier(Bezier(Point.xy(4.0, 0.0), Point.xy(4.0, 8.0), Point.xy(4.0, -8.0), Point.xy(4.0, 0.0)))
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

        bezierAssertThat(b.restrict(0.25, 0.5).asBezier).isEqualToBezier(Bezier(
                Point.xy(-1.0, 27/64.0), Point.xy(-3/4.0, 9/16.0), Point.xy(-1/2.0, 11/16.0), Point.xy(-1/4.0, 3/4.0), Point.xy(0.0, 3/4.0)))
        bezierAssertThat(b.restrict(Interval(0.25, 0.5)).asBezier).isEqualToBezier(Bezier(
                Point.xy(-1.0, 27/64.0), Point.xy(-3/4.0, 9/16.0), Point.xy(-1/2.0, 11/16.0), Point.xy(-1/4.0, 3/4.0), Point.xy(0.0, 3/4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
                .reverse()
        bezierAssertThat(r.asBezier).isEqualToBezier(Bezier(
                Point.xy(2.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(-1.0, 0.0), Point.xy(-2.0, 0.0)))
    }
    @Test
    fun testElevate() {
        println("Elevate")
        val instance = BezierDerivative(Vector(-1.0, 0.0), Vector( 0.0, 2.0), Vector(1.0, 0.0))
                .elevate()
        val expected = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1 / 3.0, 4 / 3.0), Point.xy(1 / 3.0, 4 / 3.0), Point.xy(1.0, 0.0))
        bezierAssertThat(instance.asBezier).isEqualToBezier(expected)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = BezierDerivative(Vector(-1.0, 2.0), Vector(1.0, 1.0))
                .reduce()
        val e1 = Bezier(Point.xy( 0.0, 1.5))
        bezierAssertThat(b1.asBezier).isEqualToBezier(e1)

        val b2 = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0))
                .reduce()
        val e2 = Bezier(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0))
        bezierAssertThat(b2.asBezier).isEqualToBezier(e2)

        val b3 = BezierDerivative(Vector(-1.0, 0.0), Vector(-1/3.0, 4/3.0), Vector(1/3.0, 4/3.0), Vector(1.0, 0.0))
                .reduce()
        val e3 = Bezier(Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0))
        bezierAssertThat(b3.asBezier).isEqualToBezier(e3)

        val b4 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.5, 1.0), Vector(0.0, 4/3.0), Vector(0.5, 1.0), Vector(1.0, 0.0))
                .reduce()
        val e4 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1/3.0, 4/3.0), Point.xy(1/3.0, 4/3.0), Point.xy(1.0, 0.0))
        bezierAssertThat(b4.asBezier).isEqualToBezier(e4)

        val b5 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.6, 0.8), Vector(-0.3, 1.2), Vector(0.3, 1.2), Vector(0.6, 0.8), Vector(1.0, 0.0))
                .reduce()
        val e5 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-0.5, 1.0), Point.xy( 0.0, 4/3.0), Point.xy(0.5, 1.0), Point.xy(1.0, 0.0))
        bezierAssertThat(b5.asBezier).isEqualToBezier(e5)
    }
    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (front, back) = BezierDerivative(Vector(1.0,-2.0, 0.0), Vector(2.0,-1.0, 0.0), Vector(0.0, 0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
                .subdivide(0.25)
        bezierAssertThat(front.asBezier).isEqualToBezier(Bezier(
                Point.xyz(1.0,      -2.0, 0.0),     Point.xyz(5/4.0,  -7/4.0, 0.0),     Point.xyz(21/16.0,-3/2.0, 1/8.0), Point.xyz(83/64.0,-5/4.0, 9/32.0), Point.xyz(322/256.0,-1.0, 27/64.0)))
        bezierAssertThat(back.asBezier ).isEqualToBezier(Bezier(
                Point.xyz(322/256.0,-1.0, 27/64.0), Point.xyz(73/64.0,-1/4.0, 27/32.0), Point.xyz(13/16.0, 1/2.0, 9/8.0), Point.xyz(7/4.0,   5/4.0, 0.0),    Point.xyz(1.0,       2.0, 0.0)))
    }
}