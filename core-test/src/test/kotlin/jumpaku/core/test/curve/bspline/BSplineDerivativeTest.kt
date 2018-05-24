package jumpaku.core.test.curve.bspline

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.curve.Interval
import jumpaku.core.curve.Knot
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.BSplineDerivative
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import io.vavr.collection.Array
import jumpaku.core.test.geom.shouldEqualToVector
import jumpaku.core.test.curve.bezier.shouldEqualToBezier
import jumpaku.core.test.curve.shouldEqualToInterval
import jumpaku.core.test.curve.shouldEqualToKnotVector
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldEqualTo
import org.junit.Test


class BSplineDerivativeTest {

    val b = BSplineDerivative(BSpline(
            Array.of(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))
    @Test
    fun testProperties() {
        println("Properties")

        b.controlVectors[0].shouldEqualToVector(Vector(-1.0, 0.0))
        b.controlVectors[1].shouldEqualToVector(Vector(-1.0, 1.0))
        b.controlVectors[2].shouldEqualToVector(Vector(0.0, 1.0))
        b.controlVectors[3].shouldEqualToVector(Vector(0.0, 0.0))
        b.controlVectors[4].shouldEqualToVector(Vector(1.0, 0.0))
        b.controlVectors.size().shouldEqualTo(5)

        b.knotVector.shouldEqualToKnotVector(KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        b.domain.shouldEqualToInterval(Interval(3.0, 4.0))

        b.degree.shouldEqualTo(3)
    }


    @Test
    fun testToString() {
        println("ToString")
        b.toString().parseJson().flatMap { BSplineDerivative.fromJson(it) }.get().toBSpline().shouldEqualToBSpline(b.toBSpline())
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        b.evaluate(3.0).shouldEqualToVector(Vector(-1.0, 0.00))
        b.evaluate(3.25).shouldEqualToVector(Vector(-23 / 32.0, 27 / 32.0))
        b.evaluate(3.5).shouldEqualToVector(Vector(-1 / 4.0, 3 / 4.0))
        b.evaluate(3.75).shouldEqualToVector(Vector(3 / 32.0, 9 / 32.0))
        b.evaluate(4.0).shouldEqualToVector(Vector(1.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val a = b.derivative
        val e = BSpline(
                Array.of(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 2, 7))

        a.toBSpline().shouldEqualToBSpline(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = b.restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        b0.toBSpline().shouldEqualToBSpline(e0)

        val b1 = b.restrict(3.5, 3.75)
        val e1 = BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clamped(Interval(3.5, 3.75), 3, 8))
        b1.toBSpline().shouldEqualToBSpline(e1)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = b.reverse()
        val e = BSpline(
                Array.of(Point.xy(1.0, 0.0), Point.xy(0.0, 0.0), Point.xy(0.0, 1.0), Point.xy(-1.0, 1.0), Point.xy(-1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

        r.toBSpline().shouldEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = b.toBeziers()
        beziers.get(0).toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)))
        beziers.get(1).toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)))
        beziers.size().shouldEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s00, s01) = b.subdivide(3.0)
        s00.isDefined.shouldBeFalse()
        s01.get().toBSpline().shouldEqualToBSpline(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))

        val (s10, s11) = b.subdivide(3.5)
        s10.get().toBSpline().shouldEqualToBSpline(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 4))))
        s11.get().toBSpline().shouldEqualToBSpline(BSpline(
                Array.of(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.5, 4.0), 3, 8)))

        val (s20, s21) = b.subdivide(4.0)
        s20.get().toBSpline().shouldEqualToBSpline(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))
        s21.isDefined.shouldBeFalse()
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = b.insertKnot(3.25)
        val e0 = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4)))
        b0.toBSpline().shouldEqualToBSpline(e0)

        val b1 = b.insertKnot(3.5, 2)
        val e1 = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4)))
        b1.toBSpline().shouldEqualToBSpline(e1)
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val b0 = BSplineDerivative(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.25), Knot(3.5), Knot(4.0, 4))))
                .removeKnot(3.25, 1)
        val e0 = b
        b0.toBSpline().shouldEqualToBSpline(e0.toBSpline())

        val b1 = BSplineDerivative(BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, Knot(3.0, 4), Knot(3.5, 3), Knot(4.0, 4))))
                .removeKnot(3.5, 2)
        val e1 = b
        b1.toBSpline().shouldEqualToBSpline(e1.toBSpline())
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

        c.toBSpline().shouldEqualToBSpline(e)
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

        c.toBSpline().shouldEqualToBSpline(e)
    }
}