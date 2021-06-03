package jumpaku.curves.core.test.curve.bspline

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineDerivative
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.test.geom.closeTo
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class BSplineDerivativeTest {

    val b = BSplineDerivative(
        BSpline(
            listOf(
                Point.xyr(-1.0, 0.0, 0.0),
                Point.xyr(-1.0, 1.0, 1.0),
                Point.xyr(0.0, 1.0, 2.0),
                Point.xyr(0.0, 0.0, 1.0),
                Point.xyr(1.0, 0.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )
    )

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
        val a = b.differentiate()
        val e = BSpline(
            listOf(Point.xy(0.0, 6.0), Point.xy(3.0, 0.0), Point.xy(0.0, -3.0), Point.xy(6.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 2, 7)
        )

        assertThat(a.curve, `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b0 = b.restrict(Interval(3.5, 3.75))
        val e0 = BSpline(
            listOf(
                Point.xy(-0.25, 0.75),
                Point.xy(-0.125, 5 / 8.0),
                Point.xy(-1 / 16.0, 7 / 16.0),
                Point.xy(3 / 32.0, 9 / 32.0)
            ),
            KnotVector.clamped(Interval(3.5, 3.75), 3, 8)
        )
        assertThat(b0.curve, `is`(closeTo(e0)))

        val b1 = b.restrict(3.5, 3.75)
        val e1 = BSpline(
            listOf(
                Point.xy(-0.25, 0.75),
                Point.xy(-0.125, 5 / 8.0),
                Point.xy(-1 / 16.0, 7 / 16.0),
                Point.xy(3 / 32.0, 9 / 32.0)
            ),
            KnotVector.clamped(Interval(3.5, 3.75), 3, 8)
        )
        assertThat(b1.curve, `is`(closeTo(e1)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = b.reverse()
        val e = BSpline(
            listOf(
                Point.xy(1.0, 0.0),
                Point.xy(0.0, 0.0),
                Point.xy(0.0, 1.0),
                Point.xy(-1.0, 1.0),
                Point.xy(-1.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )

        assertThat(r.curve, `is`(closeTo(e)))
    }

    @Test
    fun testToBeziers() {
        println("ToBeziers")
        val beziers = b.toBeziers()
        assertThat(
            beziers[0].curve,
            `is`(closeTo(Bezier(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75))))
        )
        assertThat(
            beziers[1].curve,
            `is`(closeTo(Bezier(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0))))
        )
        assertThat(beziers.size, `is`(2))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (s00, s01) = b.subdivide(3.0)
        val e00 = BSpline(
            listOf(
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0))
        )
        val e01 = b.curve
        assertThat(s00.curve, `is`(closeTo(e00)))
        assertThat(s01.curve, `is`(closeTo(e01)))

        val (s10, s11) = b.subdivide(3.5)
        val e10 = BSpline(
            listOf(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(-0.5, 1.0), Point.xy(-0.25, 0.75)),
            KnotVector.clamped(Interval(3.0, 3.5), 3, 8)
        )
        val e11 = BSpline(
            listOf(Point.xy(-0.25, 0.75), Point.xy(0.0, 0.5), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
            KnotVector.clamped(Interval(3.5, 4.0), 3, 8)
        )
        assertThat(s10.curve, `is`(closeTo(e10)))
        assertThat(s11.curve, `is`(closeTo(e11)))

        val (s20, s21) = b.subdivide(4.0)
        val e20 = BSpline(
            listOf(
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 1.0),
                Point.xy(0.0, 1.0),
                Point.xy(0.0, 0.0),
                Point.xy(1.0, 0.0)
            ),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)
        )
        val e21 = BSpline(
            listOf(
                Point.xy(1.0, 0.0),
                Point.xy(1.0, 0.0),
                Point.xy(1.0, 0.0),
                Point.xy(1.0, 0.0)
            ),
            KnotVector.of(3, listOf(4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(s20.curve, `is`(closeTo(e20)))
        assertThat(s21.curve, `is`(closeTo(e21)))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val b0 = b.insertKnot(3.25)
        val e0 = BSpline(
            listOf(
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 0.5),
                Point.xy(-0.75, 1.0),
                Point.xy(0.0, 0.75),
                Point.xy(0.0, 0.0),
                Point.xy(1.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.25, 3.5, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(b0.curve, `is`(closeTo(e0)))

        val b1 = b.insertKnot(3.5, 2)
        val e1 = BSpline(
            listOf(
                Point.xy(-1.0, 0.0),
                Point.xy(-1.0, 1.0),
                Point.xy(-0.5, 1.0),
                Point.xy(-0.25, 0.75),
                Point.xy(0.0, 0.5),
                Point.xy(0.0, 0.0),
                Point.xy(1.0, 0.0)
            ),
            KnotVector.of(3, listOf(3.0, 3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))
        )
        assertThat(b1.curve, `is`(closeTo(e1)))
    }

    @Test
    fun testClose() {
        println("Close")
        val c = BSplineDerivative(
            BSpline(
                listOf(
                    Point.xyr(-1.0, 0.0, 0.0),
                    Point.xyr(-1.0, 1.0, 1.0),
                    Point.xyr(0.0, 1.0, 2.0),
                    Point.xyr(0.0, 0.0, 1.0),
                    Point.xyr(1.0, 0.0, 0.0)
                ),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
            )
        )
            .close()
        val e = BSpline(
            listOf(Point.xy(0.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(0.0, 0.0)),
            KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
        )

        assertThat(c.curve, `is`(closeTo(e)))
    }
}

