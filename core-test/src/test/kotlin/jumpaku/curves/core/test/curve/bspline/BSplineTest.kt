package jumpaku.curves.core.test.curve.bspline

import io.vavr.collection.Array
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class BSplineTest {

    val clamped = BSpline(
            Array.of(
                    Point.xyr(-1.0, 0.0, 0.0),
                    Point.xyr(-1.0, 1.0, 1.0),
                    Point.xyr(0.0, 1.0, 2.0),
                    Point.xyr(0.0, 0.0, 1.0),
                    Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

    val uniform = BSpline(
            Array.of(
                    Point.xy(-1.0, 0.0),
                    Point.xy(-1.0, 1.0),
                    Point.xy(0.0, 1.0),
                    Point.xy(0.0, 0.0),
                    Point.xy(1.0, 0.0)),
            KnotVector.uniform(Interval(3.0, 4.0), 2, 8))

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(clamped.controlPoints[0], `is`(closeTo(Point.xyr(-1.0, 0.0, 0.0))))
        assertThat(clamped.controlPoints[1], `is`(closeTo(Point.xyr(-1.0, 1.0, 1.0))))
        assertThat(clamped.controlPoints[2], `is`(closeTo(Point.xyr(0.0, 1.0, 2.0))))
        assertThat(clamped.controlPoints[3], `is`(closeTo(Point.xyr(0.0, 0.0, 1.0))))
        assertThat(clamped.controlPoints[4], `is`(closeTo(Point.xyr(1.0, 0.0, 0.0))))
        assertThat(clamped.controlPoints.size, `is`(5))
        assertThat(clamped.knotVector, `is`(closeTo(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))))
        assertThat(clamped.domain, `is`(closeTo(Interval(3.0, 4.0))))
        assertThat(clamped.degree, `is`(3))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = clamped.toString().parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(clamped)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(clamped.evaluate(3.0), `is`(closeTo(Point.xyr(-1.0, 0.0, 0.0))))
        assertThat(clamped.evaluate(3.25), `is`(closeTo(Point.xyr(-23 / 32.0, 27 / 32.0, 9 / 8.0))))
        assertThat(clamped.evaluate(3.5), `is`(closeTo(Point.xyr(-1 / 4.0, 3 / 4.0, 1.5))))
        assertThat(clamped.evaluate(3.75), `is`(closeTo(Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0))))
        assertThat(clamped.evaluate(4.0), `is`(closeTo(Point.xyr(1.0, 0.0, 0.0))))

        assertThat(uniform.evaluate(3.0), `is`(closeTo(Point.xy(-1.0, 0.5))))
        assertThat(uniform.evaluate(3 + 1 / 3.0), `is`(closeTo(Point.xy(-0.5, 1.0))))
        assertThat(uniform.evaluate(3 + 2 / 3.0), `is`(closeTo(Point.xy(0.0, 0.5))))
        assertThat(uniform.evaluate(4.0), `is`(closeTo(Point.xy(0.5, 0.0))))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val e0 = BSpline(
                Array.of(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))
        assertThat(clamped.derivative.toBSpline(), `is`(closeTo(e0)))

        val e1 = BSpline(
                Array.of(Point.xy(0.0, 3.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(3.0, 0.0)),
                KnotVector.uniform(Interval(3.0, 4.0), 1, 6))
        assertThat(uniform.derivative.toBSpline(), `is`(closeTo(e1)))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = clamped.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0))))
        val e = BSpline(
                Array.of(Point.xy(1.0, -1.0), Point.xy(-1.0, -1.0), Point.xy(-1.0, 1.0), Point.xy(1.0, 1.0), Point.xy(1.0, 3.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val a = clamped.toCrisp()
        val e = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e0 = BSpline(
                Array.of(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        assertThat(clamped.restrict(Interval(3.5, 3.75)), `is`(closeTo(e0)))

        val e1 = BSpline(
                Array.of(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.125, 5 / 8.0, 1.5), Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0), Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        assertThat(clamped.restrict(3.5, 3.75), `is`(closeTo(e1)))

        assertThat(clamped.restrict(3.0, 4.0), `is`(closeTo(clamped)))

        val e2 = BSpline(
                Array.of(Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 8))
        assertThat(uniform.restrict(3.0, 4.0), `is`(closeTo(e2)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .reverse()
        val e = BSpline(
                Array.of(Point.xyr(1.0, 0.0, 0.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        assertThat(r, `is`(closeTo(e)))
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers0 = clamped.toBeziers()
        assertThat(beziers0.size, `is`(2))
        assertThat(beziers0[0], `is`(closeTo(Bezier(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)))))
        assertThat(beziers0[1], `is`(closeTo(Bezier(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)))))

        val beziers1 = uniform.toBeziers()
        assertThat(beziers1.size, `is`(3))
        assertThat(beziers1[0], `is`(closeTo(Bezier(Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0)))))
        assertThat(beziers1[1], `is`(closeTo(Bezier(Point.xy(-0.5, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.5)))))
        assertThat(beziers1[2], `is`(closeTo(Bezier(Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)))))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s01, s02) = clamped.subdivide(3.0)
        assertThat(s01.isDefined, `is`(false))
        assertThat(s02.orThrow(), `is`(closeTo(clamped)))

        val (s11, s12) = clamped.subdivide(3.5)
        assertThat(s11.orThrow(), `is`(closeTo(BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5)),
                KnotVector.clamped(Interval(3.0, 3.5), 3, 8)))))
        assertThat(s12.orThrow(), `is`(closeTo(BSpline(
                Array.of(Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))))

        val (s21, s22) = clamped.subdivide(4.0)
        assertThat(s21.orThrow(), `is`(closeTo(clamped)))
        assertThat(s22.isDefined, `is`(false))

        val (s31, s32) = uniform.subdivide(3.0)
        assertThat(s31.isDefined, `is`(false))
        assertThat(s32.orThrow(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-1.0, 0.5), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(2, Knot(3.0, 3), Knot(10/3.0), Knot(11/3.0), Knot(4.0), Knot(13/3.0), Knot(14/3.0))))))

        val (s41, s42) = uniform.subdivide(4.0)
        assertThat(s41.orThrow(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.5, 0.0)),
                KnotVector(2, Knot(7/3.0), Knot(8/3.0), Knot(3.0), Knot(10/3.0), Knot(11/3.0), Knot(4.0, 3))))))
        assertThat(s42.isDefined, `is`(false))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = clamped.insertKnot(3.25)
        val e0 = BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 0.5, 0.5), Point.xyr(-0.75, 1.0, 1.25), Point.xyr(0.0, 0.75, 1.75), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        assertThat(b0, `is`(closeTo(e0)))

        val b1 = clamped.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
        assertThat(b1, `is`(closeTo(e1)))

        val b2 = clamped.insertKnot(3.5, 3)
        val e2 = BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(-0.5, 1.0, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(-0.25, 0.75, 1.5), Point.xyr(0.0, 0.5, 1.5), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 4), Knot(4.0, 4)))
        assertThat(b2, `is`(closeTo(e2)))
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val clamped = clamped.toCrisp()

        for (times in 0..4) {
            val c = clamped.insertKnot(3.25, times).removeKnot(3.25, times)
            assertThat(c, `is`(closeTo(clamped)))
        }
        for (times in 0..3) {
            val c = clamped.insertKnot(3.5, times).removeKnot(3.5, times)
            assertThat(c, `is`(closeTo(clamped)))
        }
        for (times in 0..4) {
            val c = clamped.insertKnot(3.75, times).removeKnot(3.75, times)
            assertThat(c, `is`(closeTo(clamped)))
        }

        for (times in 0..2) {
            val u = uniform.insertKnot(3.0, times).removeKnot(3.0, times)
            assertThat(u, `is`(closeTo(uniform)))
        }
        for (times in 0..2) {
            val u = uniform.insertKnot(4.0, times).removeKnot(4.0, times)
            assertThat(u, `is`(closeTo(uniform)))
        }
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = clamped
        assertThat(c.clamp(), `is`(closeTo(c)))

        val u = BSpline(
                Array.of(
                        Point.xy(-1.0, 0.0),
                        Point.xy(-1.0, 1.0),
                        Point.xy(0.0, 1.0),
                        Point.xy(0.0, 0.0),
                        Point.xy(1.0, 0.0)),
                KnotVector.uniform(Interval(3.0, 4.0), 2, 8))
        val e = BSpline(
                Array.of(
                        Point.xy(-1.0, 0.5),
                        Point.xy(-1.0, 1.0),
                        Point.xy(0.0, 1.0),
                        Point.xy(0.0, 0.0),
                        Point.xy(0.5, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 8))
        assertThat(u.clamp(), `is`(closeTo(e)))
    }

    @Test
    fun testClose() {
        println("Close")
        val ac = clamped
        val ec = BSpline(
                Array.of(Point.xyr(0.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(0.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))
        assertThat(ac.close(), `is`(closeTo(ec)))

        val au = BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.uniform(Interval(0.0, 2.0), 2, 8))
        val eu = BSpline(
                Array.of(Point.xyr(-0.25, 0.25, 0.5), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(-0.25, 0.25, 0.5)),
                KnotVector.clamped(Interval(0.0, 2.0), 2, 8))
        assertThat(au.close(), `is`(closeTo(eu)))
    }

    @Test
    fun test_Basis() {
        println("Basis")
        val knots = KnotVector.clamped(Interval(0.0, 2.0), 2, 7)

        assertThat(BSpline.basis(0.0, 0, knots), `is`(closeTo(1.0)))
        assertThat(BSpline.basis(0.0, 1, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(0.0, 2, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(0.0, 3, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(0.5, 0, knots), `is`(closeTo(0.25)))
        assertThat(BSpline.basis(0.5, 1, knots), `is`(closeTo(5 / 8.0)))
        assertThat(BSpline.basis(0.5, 2, knots), `is`(closeTo(0.125)))
        assertThat(BSpline.basis(0.5, 3, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(1.0, 0, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(1.0, 1, knots), `is`(closeTo(0.5)))
        assertThat(BSpline.basis(1.0, 2, knots), `is`(closeTo(0.5)))
        assertThat(BSpline.basis(1.0, 3, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(1.5, 0, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(1.5, 1, knots), `is`(closeTo(0.125)))
        assertThat(BSpline.basis(1.5, 2, knots), `is`(closeTo(5 / 8.0)))
        assertThat(BSpline.basis(1.5, 3, knots), `is`(closeTo(0.25)))
        assertThat(BSpline.basis(2.0, 0, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(2.0, 1, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(2.0, 2, knots), `is`(closeTo(0.0)))
        assertThat(BSpline.basis(2.0, 3, knots), `is`(closeTo(1.0)))
    }
}