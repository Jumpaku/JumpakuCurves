package org.jumpaku.core.curve.rationalbezier

import com.github.salomonbrys.kotson.fromJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.intervalAssertThat
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.curve.polyline.polylineAssertThat
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.paramPointAssertThat
import org.jumpaku.core.json.prettyGson
import org.junit.Test



class LineSegmentTest {
    @Test
    fun testProperties() {
        println("Properties")
        val l = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        paramPointAssertThat(l.front).isEqualToParamPoint(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25))
        paramPointAssertThat(l.back).isEqualToParamPoint(ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        intervalAssertThat(l.domain).isEqualToInterval(Interval(0.0, 1.0))
        assertThat(l.degree).isEqualTo(1)
        assertThat(l.representPoints.size()).isEqualTo(2)
        pointAssertThat(l.representPoints[0]).isEqualToPoint(Point.xyr(2.0, 1.0, 2.0))
        pointAssertThat(l.representPoints[1]).isEqualToPoint(Point.xyr(3.0, 2.0, 1.0))
    }

    @Test
    fun testAsArcLengthCurve() {
        println("GetAsArcLengthCurve")
        val d = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        polylineAssertThat(d.toArcLengthCurve().polyline).isEqualToPolyline(Polyline(
                Point.xyr(1.0, 0.0, 5.0), Point.xyr(2.0, 1.0, 2.0), Point.xyr(3.0, 2.0, 1.0), Point.xyr(5.0, 4.0, 7.0)))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val d = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))


        vectorAssertThat(d.derivative(0.0 )).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.derivative(0.25)).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.derivative(0.5 )).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.derivative(0.75)).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.derivative(1.0 )).isEqualToVector(Vector(4.0, 4.0))

        vectorAssertThat(d.differentiate(0.0 )).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.differentiate(0.25)).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.differentiate(0.5 )).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.differentiate(0.75)).isEqualToVector(Vector(4.0, 4.0))
        vectorAssertThat(d.differentiate(1.0 )).isEqualToVector(Vector(4.0, 4.0))
    }

    @Test
    fun testCrispTransform() {
        println("CrispTransform")
        val d = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
                .crispTransform(Transform.ID.scale(2.0).rotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).translate(Vector(1.0, 1.0)))
        lineSegmentAssertThat(d).isEqualLineSegment(LineSegment(ParamPoint(Point.xy(-1.0, 5.0), 0.25), ParamPoint(Point.xy(-3.0, 7.0), 0.5)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val l = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))

        pointAssertThat(l.evaluate(0.0 )).isEqualToPoint(Point.xyr(1.0, 0.0, 5.0))
        pointAssertThat(l.evaluate(0.25)).isEqualToPoint(Point.xyr(2.0, 1.0, 2.0))
        pointAssertThat(l.evaluate(0.5 )).isEqualToPoint(Point.xyr(3.0, 2.0, 1.0))
        pointAssertThat(l.evaluate(0.75)).isEqualToPoint(Point.xyr(4.0, 3.0, 4.0))
        pointAssertThat(l.evaluate(1.0 )).isEqualToPoint(Point.xyr(5.0, 4.0, 7.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val l = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5))
        lineSegmentAssertThat(prettyGson.fromJson<LineSegmentJson>(l.toString()).lineSegment()).isEqualLineSegment(l)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val l = LineSegment(ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.25), ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5)).reverse()
        lineSegmentAssertThat(l).isEqualLineSegment(LineSegment(ParamPoint(Point.xyr(3.0, 2.0, 1.0), 0.5), ParamPoint(Point.xyr(2.0, 1.0, 2.0), 0.75)))
    }

}