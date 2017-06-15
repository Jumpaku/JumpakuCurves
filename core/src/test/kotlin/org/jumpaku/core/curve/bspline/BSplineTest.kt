package org.jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.Array
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.Transform
import org.jumpaku.core.affine.Vector
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.bezier.bezierAssertThat
import org.jumpaku.core.curve.knotAssertThat
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import org.junit.Test


class BSplineTest {
    @Test
    fun testProperties() {
        println("Properties")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))

        pointAssertThat(b.controlPoints[0]).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(b.controlPoints[1]).isEqualToPoint(Point.xyr(-1.0, 1.0, 1.0))
        pointAssertThat(b.controlPoints[2]).isEqualToPoint(Point.xyr(0.0, 1.0, 2.0))
        pointAssertThat(b.controlPoints[3]).isEqualToPoint(Point.xyr(0.0, 0.0, 1.0))
        pointAssertThat(b.controlPoints[4]).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
        assertThat(5).isEqualTo(b.controlPoints.size())

        knotAssertThat(b.knots[0]).isEqualToKnot(Knot(3.0, 4))
        knotAssertThat(b.knots[1]).isEqualToKnot(Knot(3.5, 1))
        knotAssertThat(b.knots[2]).isEqualToKnot(Knot(4.0, 4))
        assertThat(b.knots.size()).isEqualTo(3)

        assertThat(b.knotValues[0]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[1]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[2]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[3]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[4]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(b.knotValues[5]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[6]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[7]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[8]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(b.knotValues.size()).isEqualTo(9)

        assertThat(b.domain.begin).isEqualTo(b.knots[0].value)
        assertThat(b.domain.end).isEqualTo(b.knots[2].value)

        assertThat(b.degree).isEqualTo(3)
    }


    @Test
    fun testToString() {
        println("ToString")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
        bSplineAssertThat(prettyGson.fromJson<BSplineJson>(b.toString()).bSpline()).isEqualToBSpline(b)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val bSpline = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))

        pointAssertThat(bSpline.evaluate(3.0)).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(bSpline.evaluate(3.25)).isEqualToPoint(Point.xyr(-23 / 32.0, 27 / 32.0, 9 / 8.0))
        pointAssertThat(bSpline.evaluate(3.5)).isEqualToPoint(Point.xyr(-1 / 4.0, 3 / 4.0, 1.5))
        pointAssertThat(bSpline.evaluate(3.75)).isEqualToPoint(Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0))
        pointAssertThat(bSpline.evaluate(4.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
        val e = BSpline(
               Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 2, 7))

        bSplineAssertThat(b.derivative.asBSpline).isEqualToBSpline(e)
    }

    @Test
    fun testCrispTransform() {
        println("CrispTransform")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
        val a = b.crispTransform(Transform.ID.scale(2.0).rotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).translate(Vector(1.0, 1.0)))
        val e = BSpline(
                Array(Point.xy(1.0, -1.0), Point.xy(-1.0, -1.0), Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, 3.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }
    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                Knot.clampedUniformKnots(3.5, 3.75, 3, 8))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .restrict(3.5, 3.75)
        val e1 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                Knot.clampedUniformKnots(3.5, 3.75, 3, 8))
        bSplineAssertThat(b1).isEqualToBSpline(e1)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .reverse()
        val e = BSpline(
                Array(Point.xyr(1.0, 0.0, 0.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))

        bSplineAssertThat(r).isEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .toBeziers()
        bezierAssertThat(beziers.get(0)).isEqualToBezier(Bezier(
                Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)))
        bezierAssertThat(beziers.get(1)).isEqualToBezier(Bezier(
                Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)))
        assertThat(beziers.size()).isEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (b0, b1) = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .subdivide(3.5)
        bSplineAssertThat(b0).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)),
                Knot.clampedUniformKnots(3.0, 3.5, 3, 8)))
        bSplineAssertThat(b1).isEqualToBSpline(BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.5, 4.0, 3, 8)))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 0.5, 0.5), Point.xyr(-0.75, 1.0, 1.25), Point.xyr(0.0, 0.75, 1.75), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Array(Knot(3.0, 4), Knot(3.25, 1), Knot(3.5, 1), Knot(4.0, 4)))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3.0, 4.0, 3, 9))
                .insertKnot(4, 2)
        val e1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                Array(Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))

        bSplineAssertThat(b1).isEqualToBSpline(e1)
    }

    @Test
    fun test_Basis() {
        println("Basis")
        val knots = Knot.clampedUniformKnots(2, 7).flatMap(Knot::toArray)

        assertThat(BSpline.basis(0.0, 2, 0, knots)).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 2, 1, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 2, 2, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 2, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(0.5, 2, 0, knots)).isEqualTo(0.25,    withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 2, 1, knots)).isEqualTo(5 / 8.0, withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 2, 2, knots)).isEqualTo(0.125,   withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 2, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(1.0, 2, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 2, 1, knots)).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 2, 2, knots)).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 2, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(1.5, 2, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 2, 1, knots)).isEqualTo(0.125,   withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 2, 2, knots)).isEqualTo(5 / 8.0, withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 2, 3, knots)).isEqualTo(0.25,    withPrecision(1.0e-10))

        assertThat(BSpline.basis(2.0, 2, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 2, 1, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 2, 2, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 2, 3, knots)).isEqualTo(1.0,     withPrecision(1.0e-10))
    }

}