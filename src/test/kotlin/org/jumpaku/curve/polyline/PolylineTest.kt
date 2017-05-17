package org.jumpaku.curve.polyline

import org.assertj.core.api.Assertions.*
import org.jumpaku.util.*
import org.jumpaku.affine.Point
import org.jumpaku.affine.pointAssertThat
import org.jumpaku.curve.Interval
import org.jumpaku.jsonAssertThat
import org.junit.Test


class PolylineTest {
    @Test
    fun testProperties() {
        println("Properties")
        val p = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
        pointAssertThat(p.points[0]).isEqualToPoint(Point.xyr(-1.0, 1.0, 2.0))
        pointAssertThat(p.points[1]).isEqualToPoint(Point.xyr( 1.0, 1.0, 1.0))
        pointAssertThat(p.points[2]).isEqualToPoint(Point.xyr( 1.0,-3.0, 3.0))
        pointAssertThat(p.points[3]).isEqualToPoint(Point.xyzr( 1.0,-3.0, 1.5, 2.0))
        assertThat(p.domain.begin).isCloseTo(0.0, withPrecision(1.0e-10))
        assertThat(p.domain.end)  .isCloseTo(7.5, withPrecision(1.0e-10))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = Polyline.fromJson(
                """{"points":[{"x":-1.0,"y":1.0,"z":0.0,"r":2.0},{"x":1.0,"y":1.0,"z":0.0,"r":1.0},{"x":1.0,"y":-3.0,"z":0.0,"r":3.0},{"x":1.0,"y":-3.0,"z":1.5,"r":2.0}]}""")!!
        polylineAssertThat(p).isEqualToPolyline(
                Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0)))
        jsonAssertThat(Polyline.toJson(p)).isEqualToWithoutWhitespace(
                """{"points":[{"x":-1.0,"y":1.0,"z":0.0,"r":2.0},{"x":1.0,"y":1.0,"z":0.0,"r":1.0},{"x":1.0,"y":-3.0,"z":0.0,"r":3.0},{"x":1.0,"y":-3.0,"z":1.5,"r":2.0}]}""")
        jsonAssertThat(p.toString()).isEqualToWithoutWhitespace(
                """{"points":[{"x":-1.0,"y":1.0,"z":0.0,"r":2.0},{"x":1.0,"y":1.0,"z":0.0,"r":1.0},{"x":1.0,"y":-3.0,"z":0.0,"r":3.0},{"x":1.0,"y":-3.0,"z":1.5,"r":2.0}]}""")

        assertThat(Polyline.fromJson("""{"points":{"x":null,"y":1.0,"z":0.0,"r":2.0}]}""")).isNull()
        assertThat(Polyline.fromJson("""{"points":{"x":-1.0"y":1.0,"z":0.0,"r":2.0}]}""")).isNull()
        assertThat(Polyline.fromJson("""{"points":"x":-1.0"y":1.0,"z":0.0,"r":2.0}]}""")).isNull()
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val p = Polyline(Point.xyr(1.0, 1.0, 2.0), Point.xyr( -1.0, -1.0, 1.0), Point.xyzr(-1.0, -1.0, 1.0, 0.0))
        pointAssertThat(p.evaluate(0.0))                     .isEqualToPoint(Point.xyzr( 1.0, 1.0, 0.0, 2.0))
        pointAssertThat(p.evaluate(Math.sqrt(2.0)))          .isEqualToPoint(Point.xyzr( 0.0, 0.0, 0.0, 1.5))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0)))      .isEqualToPoint(Point.xyzr(-1.0,-1.0, 0.0, 1.0))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0) + 0.5)).isEqualToPoint(Point.xyzr(-1.0,-1.0, 0.5, 0.5))
        pointAssertThat(p.evaluate(2 * Math.sqrt(2.0) + 1))  .isEqualToPoint(Point.xyzr(-1.0,-1.0, 1.0, 0.0))
    }

    @Test
    fun testSampleArcLength() {
        println("SampleArcLength")
        val ps = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
                .sampleArcLength(6)
        assertThat(ps.size()).isEqualTo(6)
        pointAssertThat(ps[0]).isEqualToPoint(Point.xyzr(-1.0, 1.0, 0.0, 2.0 ))
        pointAssertThat(ps[1]).isEqualToPoint(Point.xyzr( 0.5, 1.0, 0.0, 1.25))
        pointAssertThat(ps[2]).isEqualToPoint(Point.xyzr( 1.0, 0.0, 0.0, 1.5 ))
        pointAssertThat(ps[3]).isEqualToPoint(Point.xyzr( 1.0,-1.5, 0.0, 2.25))
        pointAssertThat(ps[4]).isEqualToPoint(Point.xyzr( 1.0,-3.0, 0.0, 3.0 ))
        pointAssertThat(ps[5]).isEqualToPoint(Point.xyzr( 1.0,-3.0, 1.5, 2.0 ))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val p = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
                .reverse()
        polylineAssertThat(p).isEqualToPolyline(
                Polyline(Point.xyzr(1.0, -3.0, 1.5, 2.0), Point.xyr(1.0, -3.0, 3.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(-1.0, 1.0, 2.0)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val r0 = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
                .restrict(3.0, 4.5)
        polylineAssertThat(r0).isEqualToPolyline(Polyline(Point.xyr(1.0, 0.0, 1.5), Point.xyr(1.0,-1.5, 2.25)))

        val r1 = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
                .restrict(Interval(3.0, 4.5))
        polylineAssertThat(r1).isEqualToPolyline(Polyline(Point.xyr(1.0, 0.0, 1.5), Point.xyr(1.0,-1.5, 2.25)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (front, back) = Polyline(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))
                .subdivide(4.5)
        polylineAssertThat(front).isEqualToPolyline(Polyline(Point.xyr(-1.0, 1.0, 2.0 ), Point.xyr(1.0,  1.0, 1.0), Point.xyr( 1.0,-1.5, 2.25)))
        polylineAssertThat(back ).isEqualToPolyline(Polyline(Point.xyr( 1.0,-1.5, 2.25), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0)))
    }
}