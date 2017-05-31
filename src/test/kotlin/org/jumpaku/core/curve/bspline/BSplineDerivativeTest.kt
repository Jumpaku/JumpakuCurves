package org.jumpaku.curve.bspline

import io.vavr.API.Array
import org.assertj.core.api.Assertions.*
import org.jumpaku.affine.Vector
import org.jumpaku.affine.vectorAssertThat
import org.jumpaku.curve.Interval
import org.jumpaku.curve.Knot
import org.jumpaku.curve.bezier.BezierDerivative
import org.jumpaku.curve.bezier.bezierAssertThat
import org.jumpaku.curve.knotAssertThat
import org.jumpaku.util.component1
import org.jumpaku.util.component2
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/29.
 */
class BSplineDerivativeTest {
    @Test
    fun testProperties() {
        println("Properties")
        val b = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))

        vectorAssertThat(b.controlVectors[0]).isEqualToVector(Vector(-1.0, 0.0))
        vectorAssertThat(b.controlVectors[1]).isEqualToVector(Vector(-1.0, 1.0))
        vectorAssertThat(b.controlVectors[2]).isEqualToVector(Vector(0.0, 1.0))
        vectorAssertThat(b.controlVectors[3]).isEqualToVector(Vector(0.0, 0.0))
        vectorAssertThat(b.controlVectors[4]).isEqualToVector(Vector(1.0, 0.0))
        assertThat(5).isEqualTo(b.controlVectors.size())

        knotAssertThat(b.knots[0]).isEqualToKnot(Knot(0.0, 4))
        knotAssertThat(b.knots[1]).isEqualToKnot(Knot(1.0, 1))
        knotAssertThat(b.knots[2]).isEqualToKnot(Knot(2.0, 4))
        assertThat(b.knots.size()).isEqualTo(3)

        assertThat(b.knotValues[0]).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[1]).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[2]).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[3]).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[4]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[5]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[6]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[7]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(b.knotValues[8]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(b.knotValues.size()).isEqualTo(9)

        assertThat(b.domain.begin).isEqualTo(b.knots[0].value)
        assertThat(b.domain.end).isEqualTo(b.knots[2].value)

        assertThat(b.degree).isEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        val b = BSplineDerivative(
                Array(Vector(-1.0, 0.0, 0.0), Vector(-1.0, 1.0, 1.0), Vector(0.0, 1.0, 2.0), Vector(0.0, 0.0, 1.0), Vector(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
        bSplineAssertThat(BSplineDerivativeJson.fromJson(b.toString()).get().asBSpline).isEqualToBSpline(b.asBSpline)
        bSplineAssertThat(BSplineDerivativeJson.fromJson(BSplineDerivativeJson.toJson(b)).get().asBSpline).isEqualToBSpline(b.asBSpline)

        assertThat(BSplineJson.fromJson("""{"controlVectors"[{"x":0.0,"y":1.0,"z":0.0},{"x":0.0,"y":1.0,"z":0.0}],"knots":[{"value":0.0,"multiplicity":2},{"value":1.0,"multiplicity":2}]}""").isEmpty).isTrue()
        assertThat(BSplineJson.fromJson("""{"controlVectors":{"x":0.0,"y":1.0,"z":0.0},{"x":0.0,"y":1.0,"z":0.0}],"knots":[{"value":0.0,"multiplicity":2},{"value":1.0,"multiplicity":2}]}""").isEmpty).isTrue()
        assertThat(BSplineJson.fromJson("""{"controlVectors":[{"x":0.0,"y":1.0,"z":0.0},{"x":0.0,"y":1.0,"z":0.0}],"knots":[null,{"value":1.0,"multiplicity":2}]}""").isEmpty).isTrue()

    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val b = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))

        vectorAssertThat(b.evaluate(0.0)).isEqualToVector(Vector(-1.0, 0.0))
        vectorAssertThat(b.evaluate(0.5)).isEqualToVector(Vector(-23 / 32.0, 27 / 32.0))
        vectorAssertThat(b.evaluate(1.0)).isEqualToVector(Vector(-1 / 4.0, 3 / 4.0))
        vectorAssertThat(b.evaluate(1.5)).isEqualToVector(Vector(3 / 32.0, 9 / 32.0))
        vectorAssertThat(b.evaluate(2.0)).isEqualToVector(Vector(1.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val b = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
        val e = BSplineDerivative(
                Array(Vector(0.0, 3.0), Vector(1.5, 0.0), Vector(0.0, -1.5), Vector(3.0, 0.0)),
                Knot.clampedUniformKnots(2, 7))

        bSplineAssertThat(b.derivative.asBSpline).isEqualToBSpline(e.asBSpline)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .restrict(Interval(1.0, 1.5))
        val e0 = BSplineDerivative(
                Array(Vector(-0.25, 0.75), Vector(-0.125, 5 / 8.0), Vector(-1 / 16.0, 7 / 16.0), Vector(3 / 32.0, 9 / 32.0)),
                Knot.clampedUniformKnots(1.0, 1.5, 3, 8))
        bSplineAssertThat(b0.asBSpline).isEqualToBSpline(e0.asBSpline)

        val b1 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .restrict(1.0, 1.5)
        val e1 = BSplineDerivative(
                Array(Vector(-0.25, 0.75), Vector(-0.125, 5 / 8.0), Vector(-1 / 16.0, 7 / 16.0), Vector(3 / 32.0, 9 / 32.0)),
                Knot.clampedUniformKnots(1.0, 1.5, 3, 8))
        bSplineAssertThat(b1.asBSpline).isEqualToBSpline(e1.asBSpline)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSplineDerivative(
                Array(Vector(-1.0, 0.0, 0.0), Vector(-1.0, 1.0, 1.0), Vector(0.0, 1.0, 2.0), Vector(0.0, 0.0, 1.0), Vector(1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .reverse()
        val e = BSplineDerivative(
                Array(Vector(1.0, 0.0, 0.0), Vector(0.0, 0.0, 1.0), Vector(0.0, 1.0, 2.0), Vector(-1.0, 1.0, 1.0), Vector(-1.0, 0.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))

        bSplineAssertThat(r.asBSpline).isEqualToBSpline(e.asBSpline)
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .insertKnot(0.5)
        val e0 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 0.5), Vector(-0.75, 1.0), Vector(0.0, 0.75), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Array(Knot(0.0, 4), Knot(0.5, 1), Knot(1.0, 1), Knot(2.0, 4)))
        bSplineAssertThat(b0.asBSpline).isEqualToBSpline(e0.asBSpline)

        val b1 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .insertKnot(4, 2)
        val e1 = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(-0.5, 1.0), Vector(-0.25, 0.75), Vector(0.0, 0.5), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Array(Knot(0.0, 4), Knot(1.0, 3), Knot(2.0, 4)))

        bSplineAssertThat(b1.asBSpline).isEqualToBSpline(e1.asBSpline)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .toBeziers()
        bezierAssertThat(beziers[0].asBezier).isEqualToBezier(BezierDerivative(
                Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(-0.5, 1.0), Vector(-0.25, 0.75)).asBezier)
        bezierAssertThat(beziers[1].asBezier).isEqualToBezier(BezierDerivative(
                Vector(-0.25, 0.75), Vector(0.0, 0.5), Vector(0.0, 0.0), Vector(1.0, 0.0)).asBezier)
        assertThat(beziers.size()).isEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (b0, b1) = BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(0.0, 1.0), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(3, 9))
                .subdivide(1.0)
        bSplineAssertThat(b0.asBSpline).isEqualToBSpline(BSplineDerivative(
                Array(Vector(-1.0, 0.0), Vector(-1.0, 1.0), Vector(-0.5, 1.0), Vector(-0.25, 0.75)),
                Knot.clampedUniformKnots(3, 8)).asBSpline)
        bSplineAssertThat(b1.asBSpline).isEqualToBSpline(BSplineDerivative(
                Array(Vector(-0.25, 0.75), Vector(0.0, 0.5), Vector(0.0, 0.0), Vector(1.0, 0.0)),
                Knot.clampedUniformKnots(1.0, 2.0, 3, 8)).asBSpline)
    }

}