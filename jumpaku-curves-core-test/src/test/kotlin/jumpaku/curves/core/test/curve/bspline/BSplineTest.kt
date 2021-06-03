package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class BSplineTest {

    val clamped = BSpline(
        listOf(
            Point.xyr(-1.0, 0.0, 0.0),
            Point.xyr(-1.0, 1.0, 1.0),
            Point.xyr(0.0, 1.0, 2.0),
            Point.xyr(0.0, 0.0, 1.0),
            Point.xyr(1.0, 0.0, 0.0)
        ),
        KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
    )

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
    fun testEvaluate() {
        println("Evaluate")
        assertThat(clamped.evaluate(3.0), `is`(closeTo(Point.xyr(-1.0, 0.0, 0.0))))
        assertThat(clamped.evaluate(3.25), `is`(closeTo(Point.xyr(-23 / 32.0, 27 / 32.0, 9 / 8.0))))
        assertThat(clamped.evaluate(3.5), `is`(closeTo(Point.xyr(-1 / 4.0, 3 / 4.0, 1.5))))
        assertThat(clamped.evaluate(3.75), `is`(closeTo(Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0))))
        assertThat(clamped.evaluate(4.0), `is`(closeTo(Point.xyr(1.0, 0.0, 0.0))))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val e0 = BSpline(
            listOf(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 2, 7)
        )
        assertThat(clamped.differentiate().curve, `is`(closeTo(e0)))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = clamped.transform(
            UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0)))
        )
        val e = BSpline(
            listOf(
                Point.xy(1.0, -1.0),
                Point.xy(-1.0, -1.0),
                Point.xy(-1.0, 1.0),
                Point.xy(1.0, 1.0),
                Point.xy(1.0, 3.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val a = clamped.toCrisp()
        val e = BSpline(
            listOf(
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 1.0),
                Point.xy(0.0, 1.0),
                Point.xy(0.0, 0.0),
                Point.xy(1.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e0 = BSpline(
            listOf(
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(-0.125, 5 / 8.0, 1.5),
                Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0),
                Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)
            ),
            KnotVector.clamped(Interval(3.5, 3.75), 3, 8)
        )
        assertThat(clamped.restrict(Interval(3.5, 3.75)), `is`(closeTo(e0)))

        val e1 = BSpline(
            listOf(
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(-0.125, 5 / 8.0, 1.5),
                Point.xyr(-1 / 16.0, 7 / 16.0, 11 / 8.0),
                Point.xyr(3 / 32.0, 9 / 32.0, 9 / 8.0)
            ),
            KnotVector.clamped(Interval(3.5, 3.75), 3, 8)
        )
        assertThat(clamped.restrict(3.5, 3.75), `is`(closeTo(e1)))

        assertThat(clamped.restrict(3.0, 4.0), `is`(closeTo(clamped)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(0.0, 1.0, 2.0),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
        )
            .reverse()
        val e = BSpline(
            listOf(
                Point.xyr(1.0, 0.0, 0.0),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(0.0, 1.0, 2.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(-1.0, 0.0, 0.0)
            ),
            KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
        )

        assertThat(r, `is`(closeTo(e)))
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers0 = clamped.toBeziers()
        val e00 = Bezier(
            Point.xyr(-1.0, 0.0, 0.0),
            Point.xyr(-1.0, 1.0, 1.0),
            Point.xyr(-0.5, 1.0, 1.5),
            Point.xyr(-0.25, 0.75, 1.5)
        )
        val e01 = Bezier(
            Point.xyr(-0.25, 0.75, 1.5),
            Point.xyr(0.0, 0.5, 1.5),
            Point.xyr(0.0, 0.0, 1.0),
            Point.xyr(1.0, 0.0, 0.0)
        )
        assertThat(beziers0.size, `is`(2))
        assertThat(beziers0[0], `is`(closeTo(e00)))
        assertThat(beziers0[1], `is`(closeTo(e01)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s01, s02) = clamped.subdivide(3.0)
        val e01 = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 0.0, 0.0)
            ), KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0))
        )
        val e02 = clamped
        assertThat(s01, `is`(closeTo(e01)))
        assertThat(s02, `is`(closeTo(e02)))

        val (s11, s12) = clamped.subdivide(3.5)
        val e11 = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(-0.5, 1.0, 1.5),
                Point.xyr(-0.25, 0.75, 1.5)
            ),
            KnotVector.clamped(Interval(3.0, 3.5), 3, 8)
        )
        val e12 = BSpline(
            listOf(
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(0.0, 0.5, 1.5),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.5, 4.0), 3, 8)
        )
        assertThat(s11, `is`(closeTo(e11)))
        assertThat(s12, `is`(closeTo(e12)))

        val (s21, s22) = clamped.subdivide(4.0)
        val e21 = clamped
        val e22 = BSpline(
            listOf(
                Point.xyr(1.0, 0.0, 0.0),
                Point.xyr(1.0, 0.0, 0.0),
                Point.xyr(1.0, 0.0, 0.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.of(3, listOf(4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(s21, `is`(closeTo(e21)))
        assertThat(s22, `is`(closeTo(e22)))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = clamped.insertKnot(3.25)
        val e0 = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 0.5, 0.5),
                Point.xyr(-0.75, 1.0, 1.25),
                Point.xyr(0.0, 0.75, 1.75),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.25, 3.5, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(b0, `is`(closeTo(e0)))

        val b1 = clamped.insertKnot(3.5, 2)
        val e1 = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(-0.5, 1.0, 1.5),
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(0.0, 0.5, 1.5),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(b1, `is`(closeTo(e1)))

        val b2 = clamped.insertKnot(3.5, 3)
        val e2 = BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(-0.5, 1.0, 1.5),
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(-0.25, 0.75, 1.5),
                Point.xyr(0.0, 0.5, 1.5),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(b2, `is`(closeTo(e2)))
    }

    @Test
    fun testClose() {
        println("Close")
        val ac = clamped
        val ec = BSpline(
            listOf(
                Point.xyr(0.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(0.0, 1.0, 2.0),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(0.0, 0.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )
        assertThat(ac.close(), `is`(closeTo(ec)))

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

