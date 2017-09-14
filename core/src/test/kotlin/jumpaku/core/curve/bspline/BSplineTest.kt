package jumpaku.core.curve.bspline

import io.vavr.API.Array
import io.vavr.collection.Array
import jumpaku.core.affine.*
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.bezierAssertThat
import jumpaku.core.json.parseToJson
import org.junit.Test


class BSplineTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))

        pointAssertThat(b.controlPoints[0]).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(b.controlPoints[1]).isEqualToPoint(Point.xyr(-1.0, 1.0, 1.0))
        pointAssertThat(b.controlPoints[2]).isEqualToPoint(Point.xyr(0.0, 1.0, 2.0))
        pointAssertThat(b.controlPoints[3]).isEqualToPoint(Point.xyr(0.0, 0.0, 1.0))
        pointAssertThat(b.controlPoints[4]).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
        assertThat(5).isEqualTo(b.controlPoints.size())

        knotVectorAssertThat(b.knotVector).isEqualToKnotVector(KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        assertThat(b.knotVector.size()).isEqualTo(9)

        intervalAssertThat(b.domain).isEqualToInterval(Interval(3.0, 4.0))

        assertThat(b.degree).isEqualTo(3)
    }


    @Test
    fun testToString() {
        println("ToString")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        bSplineAssertThat(b.toString().parseToJson().get().bSpline).isEqualToBSpline(b)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val bSpline = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))

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
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        val e = BSpline(
                Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 2, 7))

        bSplineAssertThat(b.derivative.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testCrispTransform() {
        println("CrispTransform")
        val b = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        val a = b.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = BSpline(
                Array(Point.xy(1.0, -1.0), Point.xy(-1.0, -1.0), Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, 3.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }
    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clampedUniform(3.5, 3.75, 3, 8))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .restrict(3.5, 3.75)
        val e1 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clampedUniform(3.5, 3.75, 3, 8))
        bSplineAssertThat(b1).isEqualToBSpline(e1)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))
                .reverse()
        val e = BSpline(
                Array(Point.xyr(1.0, 0.0, 0.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(r).isEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
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
        val s0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .subdivide(3.0)
        bSplineAssertThat(s0._1()).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(-1.0, 0.0, 0.0) }),
                KnotVector.ofKnots(3, Knot(3.0, 8))))
        bSplineAssertThat(s0._2()).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))

        val s1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .subdivide(3.5)
        bSplineAssertThat(s1._1()).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.25, 0.75, 1.5)),
                KnotVector.ofKnots(3, Knot(3.0, 4), Knot(3.5, 5))))
        bSplineAssertThat(s1._2()).isEqualToBSpline(BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.5, 4.0, 3, 8)))

        val s2 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .subdivide(4.0)
        bSplineAssertThat(s2._1()).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
        bSplineAssertThat(s2._2()).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(1.0, 0.0, 0.0) }),
                KnotVector.ofKnots(3, Knot(4.0, 8))))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 0.5, 0.5), Point.xyr(-0.75, 1.0, 1.25), Point.xyr(0.0, 0.75, 1.75), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, 3.0, 3.0, 3.0, 3.0, 3.25, 3.5, 4.0, 4.0, 4.0, 4.0))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
                .insertKnot(3.5, 2)
        val e1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, 3.0, 3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))
        bSplineAssertThat(b1).isEqualToBSpline(e1)
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        val b = BSpline(Array(
                Point.xyr(0.0, 0.0, 0.0),
                Point.xyr(0.0, 600.0, 1.0),
                Point.xyr(300.0, 600.0, 2.0),
                Point.xyr(300.0, 0.0, 1.0),
                Point.xyr(600.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9))
        val a = ArcLengthAdapter(b, 1000).arcLength()
        assertThat(b.toArcLengthCurve().arcLength()).isEqualTo(a, withPrecision(0.1))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))
                .clamp()
        val e = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(c).isEqualToBSpline(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))
                .close()
        val e = BSpline(
                Array(Point.xyr(0.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(c).isEqualToBSpline(e)
    }

    @Test
    fun test_Basis() {
        println("Basis")
        val knots = KnotVector.clampedUniform(2, 7)

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