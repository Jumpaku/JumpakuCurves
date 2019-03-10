package jumpaku.curves.core.test.curve.bspline

import io.vavr.collection.Array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineDerivative
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class BSplineDerivativeTest {

    val b = BSplineDerivative(BSpline(
            Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))

    @Test
    fun testProperties() {
        println("Properties")

        assertThat(b.controlVectors[0], `is`(closeTo(Vector(-1.0, 0.0))))
        assertThat(b.controlVectors[1], `is`(closeTo(Vector(-1.0, 1.0))))
        assertThat(b.controlVectors[2], `is`(closeTo(Vector(0.0, 1.0))))
        assertThat(b.controlVectors[3], `is`(closeTo(Vector(0.0, 0.0))))
        assertThat(b.controlVectors[4], `is`(closeTo(Vector(1.0, 0.0))))
        assertThat(b.controlVectors.size, `is`(5))
        assertThat(b.knotVector, `is`(closeTo(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))))
        assertThat(b.domain, `is`(closeTo(Interval(3.0, 4.0))))
        assertThat(b.degree, `is`(3))
    }


    @Test
    fun testToString() {
        println("ToString")
        val a = b.toString().parseJson().tryMap { BSplineDerivative.fromJson(it) }.orThrow()
        assertThat(a.toBSpline(), `is`(closeTo(b.toBSpline())))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(b.evaluate(3.0), `is`(closeTo(Vector(-1.0, 0.00))))
        assertThat(b.evaluate(3.25), `is`(closeTo(Vector(-23 / 32.0, 27 / 32.0))))
        assertThat(b.evaluate(3.5), `is`(closeTo(Vector(-1 / 4.0, 3 / 4.0))))
        assertThat(b.evaluate(3.75), `is`(closeTo(Vector(3 / 32.0, 9 / 32.0))))
        assertThat(b.evaluate(4.0), `is`(closeTo(Vector(1.0, 0.0))))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val a = b.derivative
        val e = BSpline(
                Array.of(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))

        assertThat(a.toBSpline(), `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = b.restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        assertThat(b0.toBSpline(), `is`(closeTo(e0)))

        val b1 = b.restrict(3.5, 3.75)
        val e1 = BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        assertThat(b1.toBSpline(), `is`(closeTo(e1)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = b.reverse()
        val e = BSpline(
                Array.of(Point.xy(1.0, 0.0), Point.xy(0.0, 0.0), Point.xy(0.0, 1.0), Point.xy(-1.0, 1.0), Point.xy(-1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        assertThat(r.toBSpline(), `is`(closeTo(e)))
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = b.toBeziers()
        assertThat(beziers.get(0).toBezier(), `is`(closeTo(Bezier(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)))))
        assertThat(beziers.get(1).toBezier(), `is`(closeTo(Bezier(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)))))
        assertThat(beziers.size, `is`(2))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s00, s01) = b.subdivide(3.0)
        assertThat(s00.isDefined, `is`(false))
        assertThat(s01.orThrow().toBSpline(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))))

        val (s10, s11) = b.subdivide(3.5)
        assertThat(s10.orThrow().toBSpline(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 4))))))
        assertThat(s11.orThrow().toBSpline(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))))

        val (s20, s21) = b.subdivide(4.0)
        assertThat(s20.orThrow().toBSpline(), `is`(closeTo(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))))
        assertThat(s21.isDefined, `is`(false))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = b.insertKnot(3.25)
        val e0 = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        assertThat(b0.toBSpline(), `is`(closeTo(e0)))

        val b1 = b.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
        assertThat(b1.toBSpline(), `is`(closeTo(e1)))
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val b0 = BSplineDerivative(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4))))
                .removeKnot(3.25, 1)
        val e0 = b
        assertThat(b0.toBSpline(), `is`(closeTo(e0.toBSpline())))

        val b1 = BSplineDerivative(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4))))
                .removeKnot(3.5, 2)
        val e1 = b
        assertThat(b1.toBSpline(), `is`(closeTo(e1.toBSpline())))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = BSplineDerivative(BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9)))
                .clamp()
        val e = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        assertThat(c.toBSpline(), `is`(closeTo(e)))
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSplineDerivative(BSpline(
                Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9)))
                .close()
        val e = BSpline(
                Array.of(Point.xy(0.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.0, 0.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        assertThat(c.toBSpline(), `is`(closeTo(e)))
    }
}