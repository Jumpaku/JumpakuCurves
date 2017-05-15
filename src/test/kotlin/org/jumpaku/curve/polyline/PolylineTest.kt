package org.jumpaku.curve.polyline

import org.assertj.core.api.Assertions.*
import org.jumpaku.util.*
import org.jumpaku.affine.Fuzzy
import org.jumpaku.affine.pointAssertThat
import org.jumpaku.curve.Interval
import org.jumpaku.jsonAssertThat
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/14.
 */
class PolylineTest {
    @Test
    fun testProperties() {
        println("Properties")
        val p = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5))
        pointAssertThat(p.points[0]).isEqualToPoint(Fuzzy( 2.0,-1.0, 1.0))
        pointAssertThat(p.points[1]).isEqualToPoint(Fuzzy( 1.0, 1.0, 1.0))
        pointAssertThat(p.points[2]).isEqualToPoint(Fuzzy( 3.0, 1.0,-3.0))
        pointAssertThat(p.points[3]).isEqualToPoint(Fuzzy( 2.0, 1.0,-3.0, 1.5))
        assertThat(p.domain.begin).isCloseTo(0.0, withPrecision(1.0e-10))
        assertThat(p.domain.end)  .isCloseTo(7.5, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = Polyline.fromJson(
                """{"points":[{"r":2.0,"x":-1.0,"y":1.0,"z":0.0},{"r":1.0,"x":1.0,"y":1.0,"z":0.0},{"r":3.0,"x":1.0,"y":-3.0,"z":0.0},{"r":2.0,"x":1.0,"y":-3.0,"z":1.5}]}""")!!
        polylineAssertThat(p).isEqualToPolyline(
                Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5)))
        jsonAssertThat(Polyline.toJson(p)).isEqualToWithoutWhitespace(
                """{"points":[{"r":2.0,"x":-1.0,"y":1.0,"z":0.0},{"r":1.0,"x":1.0,"y":1.0,"z":0.0},{"r":3.0,"x":1.0,"y":-3.0,"z":0.0},{"r":2.0,"x":1.0,"y":-3.0,"z":1.5}]}""")
        jsonAssertThat(p.toString()).isEqualToWithoutWhitespace(
                """{"points":[{"r":2.0,"x":-1.0,"y":1.0,"z":0.0},{"r":1.0,"x":1.0,"y":1.0,"z":0.0},{"r":3.0,"x":1.0,"y":-3.0,"z":0.0},{"r":2.0,"x":1.0,"y":-3.0,"z":1.5}]}""")

        assertThat(Polyline.fromJson("""{"points":{"r":2.0,"x":null,"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(Polyline.fromJson("""{"points":{"r":2.0"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
        assertThat(Polyline.fromJson("""{"points":"r":2.0,"x":-1.0"y":1.0,"z":0.0}]}""")).isNull()
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val p = Polyline(Fuzzy(2.0, 1.0, 1.0), Fuzzy(1.0, -1.0, -1.0), Fuzzy(0.0, -1.0, -1.0, 1.0))
        pointAssertThat(p.evaluate(0.0))                     .isEqualToPoint(Fuzzy(2.0, 1.0, 1.0, 0.0))
        pointAssertThat(p.evaluate(Math.sqrt(2.0)))          .isEqualToPoint(Fuzzy(1.5, 0.0, 0.0, 0.0))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0)))      .isEqualToPoint(Fuzzy(1.0,-1.0,-1.0, 0.0))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0) + 0.5)).isEqualToPoint(Fuzzy(0.5,-1.0,-1.0, 0.5))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0) + 1))  .isEqualToPoint(Fuzzy(0.0,-1.0,-1.0, 1.0))
    }

    @Test
    fun testSampleArcLength() {
        println("SampleArcLength")
        val result = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5))
                .sampleArcLength(6)
        assertThat(result.size()).isEqualTo(6)
        pointAssertThat(result[0]).isEqualToPoint(Fuzzy(2.0 ,-1.0, 1.0, 0.0))
        pointAssertThat(result[1]).isEqualToPoint(Fuzzy(1.25, 0.5, 1.0, 0.0))
        pointAssertThat(result[2]).isEqualToPoint(Fuzzy(1.5 , 1.0, 0.0, 0.0))
        pointAssertThat(result[3]).isEqualToPoint(Fuzzy(2.25, 1.0,-1.5, 0.0))
        pointAssertThat(result[4]).isEqualToPoint(Fuzzy(3.0 , 1.0,-3.0, 0.0))
        pointAssertThat(result[5]).isEqualToPoint(Fuzzy(2.0 , 1.0,-3.0, 1.5))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val p = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5)).reverse()
        polylineAssertThat(p).isEqualToPolyline(
                Polyline(Fuzzy(2.0, 1.0, -3.0, 1.5), Fuzzy(3.0, 1.0, -3.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(2.0, -1.0, 1.0)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val r0 = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5))
                .restrict(3.0, 4.5)
        polylineAssertThat(r0).isEqualToPolyline(Polyline(Fuzzy(1.5 , 1.0, 0.0), Fuzzy(2.25, 1.0,-1.5)))

        val r1 = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5))
                .restrict(Interval(3.0, 4.5))
        polylineAssertThat(r1).isEqualToPolyline(Polyline(Fuzzy(1.5 , 1.0, 0.0), Fuzzy(2.25, 1.0,-1.5)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (front, back) = Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5))
                .subdivide(4.5)
        polylineAssertThat(front).isEqualToPolyline(Polyline(Fuzzy(2.0, -1.0, 1.0), Fuzzy(1.0, 1.0, 1.0), Fuzzy(2.25, 1.0,-1.5)))
        polylineAssertThat(back).isEqualToPolyline(Polyline(Fuzzy(2.25, 1.0,-1.5), Fuzzy(3.0, 1.0, -3.0), Fuzzy(2.0, 1.0, -3.0, 1.5)))
    }
}