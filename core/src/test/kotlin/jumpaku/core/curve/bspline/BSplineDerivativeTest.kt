package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.Array
import io.vavr.collection.Array
import org.assertj.core.api.Assertions.*
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.vectorAssertThat
import jumpaku.core.curve.*
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.bezierAssertThat
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.junit.Test


class BSplineDerivativeTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))

        vectorAssertThat(b.controlVectors[0]).isEqualToVector(Vector(-1.0, 0.0))
        vectorAssertThat(b.controlVectors[1]).isEqualToVector(Vector(-1.0, 1.0))
        vectorAssertThat(b.controlVectors[2]).isEqualToVector(Vector(0.0, 1.0))
        vectorAssertThat(b.controlVectors[3]).isEqualToVector(Vector(0.0, 0.0))
        vectorAssertThat(b.controlVectors[4]).isEqualToVector(Vector(1.0, 0.0))
        assertThat(5).isEqualTo(b.controlVectors.size())

        knotVectorAssertThat(b.knotVector).isEqualToKnotVector(KnotVector.clampedUniform(3.0, 4.0, 3, 9))

        intervalAssertThat(b.domain).isEqualToInterval(Interval(3.0, 4.0))

        assertThat(b.degree).isEqualTo(3)
    }


    @Test
    fun testToString() {
        println("ToString")
        val b = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
        bSplineAssertThat(b.toString().parseToJson().get().bSplineDerivative.toBSpline())
                .isEqualToBSpline(BSpline(
                        Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                        KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val bSpline = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))

        vectorAssertThat(bSpline.evaluate(3.0)).isEqualToVector(Vector(-1.0, 0.00))
        vectorAssertThat(bSpline.evaluate(3.25)).isEqualToVector(Vector(-23 / 32.0, 27 / 32.0))
        vectorAssertThat(bSpline.evaluate(3.5)).isEqualToVector(Vector(-1 / 4.0, 3 / 4.0))
        vectorAssertThat(bSpline.evaluate(3.75)).isEqualToVector(Vector(3 / 32.0, 9 / 32.0))
        vectorAssertThat(bSpline.evaluate(4.0)).isEqualToVector(Vector(1.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val b = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
        val e = BSpline(
                Array(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 2, 7))

        bSplineAssertThat(b.derivative.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clampedUniform(3.5, 3.75, 3, 8))
        bSplineAssertThat(b0.toBSpline()).isEqualToBSpline(e0)

        val b1 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .restrict(3.5, 3.75)
        val e1 = BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(-0.125, 5 / 8.0), Point.xy(-1 / 16.0, 7 / 16.0), Point.xy(3 / 32.0, 9 / 32.0)),
                KnotVector.clampedUniform(3.5, 3.75, 3, 8))
        bSplineAssertThat(b1.toBSpline()).isEqualToBSpline(e1)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9)))
                .reverse()
        val e = BSpline(
                Array(Point.xy(1.0, 0.0), Point.xy(0.0, 0.0), Point.xy(0.0, 1.0), Point.xy(-1.0, 1.0), Point.xy(-1.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(r.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .toBeziers()
        bezierAssertThat(beziers.get(0).toBezier()).isEqualToBezier(Bezier(
                Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)))
        bezierAssertThat(beziers.get(1).toBezier()).isEqualToBezier(Bezier(
                Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)))
        assertThat(beziers.size()).isEqualTo(2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val s0 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .subdivide(3.0)
        bSplineAssertThat(s0._1().toBSpline()).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xy(-1.0, 0.0) }),
                KnotVector.ofKnots(3, Knot(3.0, 8))))
        bSplineAssertThat(s0._2().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))

        val s1 = BSplineDerivative(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .subdivide(3.5)
        bSplineAssertThat(s1._1().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(-0.25, 0.75)),
                KnotVector.ofKnots(3, Knot(3.0, 4), Knot(3.5, 5))))
        bSplineAssertThat(s1._2().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clampedUniform(3.5, 4.0, 3, 8)))

        val s2 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .subdivide(4.0)
        bSplineAssertThat(s2._1().toBSpline()).isEqualToBSpline(BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
        bSplineAssertThat(s2._2().toBSpline()).isEqualToBSpline(BSpline(
                Array.fill(4, { Point.xy(1.0, 0.0) }),
                KnotVector.ofKnots(3, Knot(4.0, 8))))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .insertKnot(3.25)
        val e0 = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 0.5), Point.xy(-0.75, 1.0), Point.xy(0.0, 0.75), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, 3.0, 3.0, 3.0, 3.0, 3.25, 3.5, 4.0, 4.0, 4.0, 4.0))
        bSplineAssertThat(b0.toBSpline()).isEqualToBSpline(e0)

        val b1 = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3.0, 4.0, 3, 9)))
                .insertKnot(3.5, 2)
        val e1 = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector(3, 3.0, 3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))
        bSplineAssertThat(b1.toBSpline()).isEqualToBSpline(e1)
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9)))
                .clamp()
        val e = BSpline(
                Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(c.toBSpline()).isEqualToBSpline(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSplineDerivative(BSpline(
                Array(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
                KnotVector.clampedUniform(3, 9)))
                .close()
        val e = BSpline(
                Array(Point.xy(0.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.0, 0.0)),
                KnotVector.clampedUniform(3, 9))

        bSplineAssertThat(c.toBSpline()).isEqualToBSpline(e)
    }
}