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

    val knotVector = KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
    val controlPoints = Array(
            Point.xyr(-1.0, 0.0, 0.0),
            Point.xyr(-1.0, 1.0, 1.0),
            Point.xyr(0.0, 1.0, 2.0),
            Point.xyr(0.0, 0.0, 1.0),
            Point.xyr(1.0, 0.0, 0.0))
    val b = BSpline(controlPoints, knotVector)

    @Test
    fun testProperties() {
        println("Properties")
        pointAssertThat(b.controlPoints[0]).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(b.controlPoints[1]).isEqualToPoint(Point.xyr(-1.0, 1.0, 1.0))
        pointAssertThat(b.controlPoints[2]).isEqualToPoint(Point.xyr(0.0, 1.0, 2.0))
        pointAssertThat(b.controlPoints[3]).isEqualToPoint(Point.xyr(0.0, 0.0, 1.0))
        pointAssertThat(b.controlPoints[4]).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
        assertThat(b.controlPoints.size()).isEqualTo(5)

        knotVectorAssertThat(b.knotVector).isEqualToKnotVector(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        intervalAssertThat(b.domain).isEqualToInterval(Interval(3.0, 4.0))

        assertThat(b.degree).isEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        bSplineAssertThat(b.toString().parseJson().flatMap { BSpline.fromJson(it) }.get()).isEqualToBSpline(b)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        pointAssertThat(b.evaluate(3.0)).isEqualToPoint(Point.xyr(-1.0, 0.0, 0.0))
        pointAssertThat(b.evaluate(3.25)).isEqualToPoint(Point.xyr(-23 / 32.0, 27 / 32.0, 9 / 8.0))
        pointAssertThat(b.evaluate(3.5)).isEqualToPoint(Point.xyr(-1 / 4.0, 3 / 4.0, 1.5))
        pointAssertThat(b.evaluate(3.75)).isEqualToPoint(Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0))
        pointAssertThat(b.evaluate(4.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val e = BSpline(
                Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))

        bSplineAssertThat(b.derivative.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = b.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = BSpline(
                Array(Point.xy(1.0, -1.0), Point.xy(-1.0, -1.0), Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, 3.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val a = b.toCrisp()
        val e = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        bSplineAssertThat(a).isEqualToBSpline(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = b.restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = b.restrict(3.5, 3.75)
        val e1 = BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        bSplineAssertThat(b1).isEqualToBSpline(e1)
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
        val beziers = b.toBeziers()
        bezierAssertThat(beziers[0]).isEqualToBezier(Bezier(
                Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)))
        bezierAssertThat(beziers[1]).isEqualToBezier(Bezier(
                Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)))
        assertThat(beziers.size()).isEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s01, s02) = b.subdivide(3.0)
        bSplineAssertThat(s01).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(-1.0, 0.0, 0.0) }),
                KnotVector(3, Knot(3.0, 4), Knot(3.0, 4))))
        bSplineAssertThat(s02).isEqualToBSpline(b)

        val (s11, s12) = b.subdivide(3.5)
        bSplineAssertThat(s11).isEqualToBSpline(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)),
                KnotVector.clamped(Interval(3.0, 3.5), 3, 8)))
        bSplineAssertThat(s12).isEqualToBSpline(BSpline(
                Array(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))

        val (s21, s22) = b.subdivide(4.0)
        bSplineAssertThat(s21).isEqualToBSpline(b)
        bSplineAssertThat(s22).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xyr(1.0, 0.0, 0.0) }),
                KnotVector(3, Knot(4.0, 4), Knot(4.0, 4))))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = b.insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 0.5, 0.5), Point.xyr(-0.75, 1.0, 1.25), Point.xyr(0.0, 0.75, 1.75), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        bSplineAssertThat(b0).isEqualToBSpline(e0)

        val b1 = b.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
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
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        val a = ArcLengthReparametrized(b, 1000).arcLength()
        assertThat(b.reparametrizeArcLength().arcLength()).isEqualTo(a, withPrecision(0.1))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .clamp()
        val e = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        bSplineAssertThat(c).isEqualToBSpline(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .close()
        val e = BSpline(
                Array(Point.xyr(0.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        bSplineAssertThat(c).isEqualToBSpline(e)
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