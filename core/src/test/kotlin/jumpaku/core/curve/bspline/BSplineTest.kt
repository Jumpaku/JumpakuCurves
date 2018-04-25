package jumpaku.core.curve.bspline

import io.vavr.API.Array
import io.vavr.collection.Array
import jumpaku.core.affine.*
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.bezierAssertThat
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.junit.Test

fun bSplineAssertThat(actual: BSpline): BSplineAssert = BSplineAssert(actual)

class BSplineAssert(actual: BSpline) : AbstractAssert<BSplineAssert, BSpline>(actual, BSplineAssert::class.java) {

    fun isEqualToBSpline(expected: BSpline, eps: Double = 1.0e-10): BSplineAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("controlPoints size").isEqualTo(expected.controlPoints.size())

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bSpline.controlPoints[%d]", i).isEqualToPoint(e, eps)
                }

        Assertions.assertThat(actual.knotVector.knots.size()).`as`("knotVector size").isEqualTo(expected.knotVector.knots.size())

        knotVectorAssertThat(actual.knotVector).isEqualToKnotVector(expected.knotVector)

        return this
    }
}

class BSplineTest {

    val clamped = BSpline(
            Array(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(0.0, 1.0, 2.0),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

    val uniform = BSpline(
            Array(
                    Point.xy(-1.0, 0.0),
                    Point.xy(-1.0, 1.0),
                    Point.xy(0.0, 1.0),
                    Point.xy(0.0, 0.0),
                    Point.xy(1.0, 0.0)),
            KnotVector.uniform(Interval(3.0, 4.0), 2, 8))

    @Test
    fun testProperties() {
        println("Properties")
        pointAssertThat(clamped.controlPoints[0]).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(clamped.controlPoints[1]).isEqualToPoint(Point.xyr(-1.0, 1.0, 1.0))
        pointAssertThat(clamped.controlPoints[2]).isEqualToPoint(Point.xyr(0.0, 1.0, 2.0))
        pointAssertThat(clamped.controlPoints[3]).isEqualToPoint(Point.xyr(0.0, 0.0, 1.0))
        pointAssertThat(clamped.controlPoints[4]).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
        assertThat(clamped.controlPoints.size()).isEqualTo(5)

        knotVectorAssertThat(clamped.knotVector).isEqualToKnotVector(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        intervalAssertThat(clamped.domain).isEqualToInterval(Interval(3.0, 4.0))

        assertThat(clamped.degree).isEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        bSplineAssertThat(clamped.toString().parseJson().flatMap { BSpline.fromJson(it) }.get()).isEqualToBSpline(clamped)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        pointAssertThat(clamped.evaluate(3.0)).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(clamped.evaluate(3.25)).isEqualToPoint(Point.xyr(-23 / 32.0, 27 / 32.0, 9 / 8.0))
        pointAssertThat(clamped.evaluate(3.5)).isEqualToPoint(Point.xyr(-1 / 4.0, 3 / 4.0, 1.5))
        pointAssertThat(clamped.evaluate(3.75)).isEqualToPoint(Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0))
        pointAssertThat(clamped.evaluate(4.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))

        pointAssertThat(uniform.evaluate(3.0)).isEqualToPoint(Point.xy(-1.0, 0.5))
        pointAssertThat(uniform.evaluate(3 + 1/3.0)).isEqualToPoint(Point.xy(-0.5, 1.0))
        pointAssertThat(uniform.evaluate(3 + 2/3.0)).isEqualToPoint(Point.xy(0.0, 0.5))
        pointAssertThat(uniform.evaluate(4.0)).isEqualToPoint(Point.xy(0.5, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val e0 = BSpline(
                Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))
        bSplineAssertThat(clamped.derivative.toBSpline()).isEqualToBSpline(e0)

        val e1 = BSpline(
                Array(Point.xy(0.0, 3.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(3.0, 0.0)),
                KnotVector.uniform(Interval(3.0, 4.0), 1, 6))
        bSplineAssertThat(uniform.derivative.toBSpline()).isEqualToBSpline(e1)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = clamped.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = BSpline(
                Array(Point.xy(1.0, -1.0), Point.xy(-1.0, -1.0), Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, 3.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val a = clamped.toCrisp()
        val e = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e0 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(clamped.restrict(Interval(3.5, 3.75))).isEqualToBSpline(e0)

        val e1 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(clamped.restrict(3.5, 3.75)).isEqualToBSpline(e1)

        bSplineAssertThat(clamped.restrict(3.0, 4.0)).isEqualToBSpline(clamped)

        val e2 = BSpline(
                Array(Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 8))
        bSplineAssertThat(uniform.restrict(3.0, 4.0)).isEqualToBSpline(e2)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .reverse()
        val e = BSpline(
                Array(Point.xyr(1.0, 0.0, 0.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        bSplineAssertThat(r).isEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers0 = clamped.toBeziers()
        assertThat(beziers0.size()).isEqualTo(2)
        bezierAssertThat(beziers0[0]).isEqualToBezier(Bezier(
                Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)))
        bezierAssertThat(beziers0[1]).isEqualToBezier(Bezier(
                Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)))

        val beziers1 = uniform.toBeziers()
        assertThat(beziers1.size()).isEqualTo(3)
        bezierAssertThat(beziers1[0]).isEqualToBezier(Bezier(
                Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0)))
        bezierAssertThat(beziers1[1]).isEqualToBezier(Bezier(
                Point.xy(-0.5, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.5)))
        bezierAssertThat(beziers1[2]).isEqualToBezier(Bezier(
                Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s01, s02) = clamped.subdivide(3.0)
        bSplineAssertThat(s01).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(-1.0, 0.0, 0.0) }),
                KnotVector(3, Knot(3.0, 4), Knot(3.0, 4))))
        bSplineAssertThat(s02).isEqualToBSpline(clamped)

        val (s11, s12) = clamped.subdivide(3.5)
        bSplineAssertThat(s11).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)),
                KnotVector.clamped(Interval(3.0, 3.5), 3, 8)))
        bSplineAssertThat(s12).isEqualToBSpline(BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))

        val (s21, s22) = clamped.subdivide(4.0)
        bSplineAssertThat(s21).isEqualToBSpline(clamped)
        bSplineAssertThat(s22).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(1.0, 0.0, 0.0) }),
                KnotVector(3, Knot(4.0, 4), Knot(4.0, 4))))

        val (s31, s32) = uniform.subdivide(3.0)
        bSplineAssertThat(s31).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-1.0, 0.5)),
                KnotVector(2, Knot(7/3.0), Knot(8/3.0), Knot(3.0), Knot(3.0, 3))))
        bSplineAssertThat(s32).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(2, Knot(3.0, 3), Knot(10/3.0), Knot(11/3.0), Knot(4.0), Knot(13/3.0), Knot(14/3.0))))

        val (s41, s42) = uniform.subdivide(4.0)
        bSplineAssertThat(s41).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)),
                KnotVector(2, Knot(7/3.0), Knot(8/3.0), Knot(3.0), Knot(10/3.0), Knot(11/3.0), Knot(4.0, 3))))
        bSplineAssertThat(s42).isEqualToBSpline(BSpline(
                Array(Point.xy(0.5, 0.0), Point.xy(0.5, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(2, Knot(4.0, 3), Knot(4.0), Knot(13/3.0), Knot(14/3.0))))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = clamped.insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 0.5, 0.5), Point.xyr(-0.75, 1.0, 1.25), Point.xyr(0.0, 0.75, 1.75), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = clamped.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
        bSplineAssertThat(b1).isEqualToBSpline(e1)

        val b2 = clamped.insertKnot(3.5, 3)
        val e2 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 4), Knot(4.0, 4)))
        bSplineAssertThat(b2).isEqualToBSpline(e2)
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val clamped = clamped.toCrisp()

        for (times in 0..4) {
            val c = clamped.insertKnot(3.25, times).removeKnot(3.25, times)
            bSplineAssertThat(c).isEqualToBSpline(clamped)
        }
        for (times in 0..3) {
            val c = clamped.insertKnot(3.5, times).removeKnot(3.5, times)
            bSplineAssertThat(c).isEqualToBSpline(clamped)
        }
        for (times in 0..4) {
            val c = clamped.insertKnot(3.75, times).removeKnot(3.75, times)
            bSplineAssertThat(c).isEqualToBSpline(clamped)
        }

        for (times in 0..2) {
            val u = uniform.insertKnot(3.0, times).removeKnot(3.0, times)
            bSplineAssertThat(u).isEqualToBSpline(uniform)
        }
        for (times in 0..2) {
            val u = uniform.insertKnot(4.0, times).removeKnot(4.0, times)
            bSplineAssertThat(u).isEqualToBSpline(uniform)
        }
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
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        val a = ArcLengthReparametrized(b, 1000).arcLength()
        assertThat(b.reparametrizeArcLength().arcLength()).isEqualTo(a, withPrecision(0.1))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = clamped
        bSplineAssertThat(c.clamp()).isEqualToBSpline(c)

        val u = BSpline(
                Array(
                        Point.xy(-1.0, 0.0),
                        Point.xy(-1.0, 1.0),
                        Point.xy(0.0, 1.0),
                        Point.xy(0.0, 0.0),
                        Point.xy(1.0, 0.0)),
                KnotVector.uniform(Interval(3.0, 4.0), 2, 8))
        val e = BSpline(
                Array(
                        Point.xy(-1.0, 0.5),
                        Point.xy(-1.0, 1.0),
                        Point.xy(0.0, 1.0),
                        Point.xy(0.0, 0.0),
                        Point.xy(0.5, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 8))
        bSplineAssertThat(u.clamp()).isEqualToBSpline(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val ac = clamped
        val ec = BSpline(
                Array(Point.xyr(0.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        bSplineAssertThat(ac.close()).isEqualToBSpline(ec)

        val au = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.uniform(Interval(0.0, 2.0), 2, 8))
        val eu = BSpline(
                Array(Point.xyr(-0.25, 0.25, 0.5), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(-0.25, 0.25, 0.5)),
                KnotVector.clamped(Interval(0.0, 2.0), 2, 8))
        bSplineAssertThat(au.close()).isEqualToBSpline(eu)
    }

    @Test
    fun test_Basis() {
        println("Basis")
        val knots = KnotVector.clamped(Interval(0.0, 2.0), 2, 7)

        assertThat(BSpline.basis(0.0, 0, knots)).isEqualTo(1.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 1, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 2, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.0, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(0.5, 0, knots)).isEqualTo(0.25,    withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 1, knots)).isEqualTo(5 / 8.0, withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 2, knots)).isEqualTo(0.125,   withPrecision(1.0e-10))
        assertThat(BSpline.basis(0.5, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(1.0, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 1, knots)).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 2, knots)).isEqualTo(0.5,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.0, 3, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))

        assertThat(BSpline.basis(1.5, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 1, knots)).isEqualTo(0.125,   withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 2, knots)).isEqualTo(5 / 8.0, withPrecision(1.0e-10))
        assertThat(BSpline.basis(1.5, 3, knots)).isEqualTo(0.25,    withPrecision(1.0e-10))

        assertThat(BSpline.basis(2.0, 0, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 1, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 2, knots)).isEqualTo(0.0,     withPrecision(1.0e-10))
        assertThat(BSpline.basis(2.0, 3, knots)).isEqualTo(1.0,     withPrecision(1.0e-10))
    }
}