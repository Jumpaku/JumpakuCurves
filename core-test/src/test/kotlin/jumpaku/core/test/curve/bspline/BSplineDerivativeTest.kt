package jumpaku.core.test.curve.bspline

import io.vavr.API.Array
import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.curve.*
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.BSplineDerivative
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.vectorAssertThat
import jumpaku.core.test.curve.bezier.bezierAssertThat
import jumpaku.core.test.curve.intervalAssertThat
import jumpaku.core.test.curve.knotVectorAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.junit.Test


class BSplineDerivativeTest {

    val b = BSplineDerivative(BSpline(
            Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))
    @Test
    fun testProperties() {
        println("Properties")

        vectorAssertThat(b.controlVectors[0]).isEqualToVector(Vector(-1.0, 0.0))
        vectorAssertThat(b.controlVectors[1]).isEqualToVector(Vector(-1.0, 1.0))
        vectorAssertThat(b.controlVectors[2]).isEqualToVector(Vector(0.0, 1.0))
        vectorAssertThat(b.controlVectors[3]).isEqualToVector(Vector(0.0, 0.0))
        vectorAssertThat(b.controlVectors[4]).isEqualToVector(Vector(1.0, 0.0))
        assertThat(5).isEqualTo(b.controlVectors.size())

        knotVectorAssertThat(b.knotVector).isEqualToKnotVector(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        intervalAssertThat(b.domain).isEqualToInterval(Interval(3.0, 4.0))

        assertThat(b.degree).isEqualTo(3)
    }


    @Test
    fun testToString() {
        println("ToString")
        bSplineAssertThat(b.toString().parseJson().flatMap { BSplineDerivative.fromJson(it) }.get().toBSpline())
                .isEqualToBSpline(b.toBSpline())
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        vectorAssertThat(b.evaluate(3.0)).isEqualToVector(Vector(-1.0, 0.00))
        vectorAssertThat(b.evaluate(3.25)).isEqualToVector(Vector(-23 / 32.0, 27 / 32.0))
        vectorAssertThat(b.evaluate(3.5)).isEqualToVector(Vector(-1 / 4.0, 3 / 4.0))
        vectorAssertThat(b.evaluate(3.75)).isEqualToVector(Vector(3 / 32.0, 9 / 32.0))
        vectorAssertThat(b.evaluate(4.0)).isEqualToVector(Vector(1.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val a = b.derivative
        val e = BSpline(
                Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))

        bSplineAssertThat(a.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = b.restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(b0.toBSpline()).isEqualToBSpline(e0)

        val b1 = b.restrict(3.5, 3.75)
        val e1 = BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(b1.toBSpline()).isEqualToBSpline(e1)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = b.reverse()
        val e = BSpline(
                Array(Point.xy(1.0, 0.0), Point.xy(0.0, 0.0), Point.xy(0.0, 1.0), Point.xy(-1.0, 1.0), Point.xy(-1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        bSplineAssertThat(r.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = b.toBeziers()
        bezierAssertThat(beziers.get(0).toBezier()).isEqualToBezier(Bezier(
                Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)))
        bezierAssertThat(beziers.get(1).toBezier()).isEqualToBezier(Bezier(
                Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)))
        assertThat(beziers.size()).isEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s00, s01) = b.subdivide(3.0)
        assertThat(s00.isDefined).isFalse()
        bSplineAssertThat(s01.get().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))

        val (s10, s11) = b.subdivide(3.5)
        bSplineAssertThat(s10.get().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 4))))
        bSplineAssertThat(s11.get().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))

        val (s20, s21) = b.subdivide(4.0)
        bSplineAssertThat(s20.get().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))
        assertThat(s21.isDefined).isFalse()
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = b.insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        bSplineAssertThat(b0.toBSpline()).isEqualToBSpline(e0)

        val b1 = b.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
        bSplineAssertThat(b1.toBSpline()).isEqualToBSpline(e1)
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val b0 = BSplineDerivative(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4))))
                .removeKnot(3.25, 1)
        val e0 = b
        bSplineAssertThat(b0.toBSpline()).isEqualToBSpline(e0.toBSpline())

        val b1 = BSplineDerivative(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4))))
                .removeKnot(3.5, 2)
        val e1 = b
        bSplineAssertThat(b1.toBSpline()).isEqualToBSpline(e1.toBSpline())
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9)))
                .clamp()
        val e = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        bSplineAssertThat(c.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9)))
                .close()
        val e = BSpline(
                Array(Point.xy(0.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        bSplineAssertThat(c.toBSpline()).isEqualToBSpline(e)
    }
}