package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.bspline.Nurbs
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.transform.asSimilarity
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test


class NurbsTest {

    val n = Nurbs(
        listOf(
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
        ),
        KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
    )

    @Test
    fun testProperties() {
        println("Properties")

        assertThat(n.weightedControlPoints.size, `is`(6))
        assertThat(n.weightedControlPoints[0], `is`(closeTo(WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0))))
        assertThat(n.weightedControlPoints[1], `is`(closeTo(WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0))))
        assertThat(n.weightedControlPoints[2], `is`(closeTo(WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0))))
        assertThat(n.weightedControlPoints[3], `is`(closeTo(WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0))))
        assertThat(n.weightedControlPoints[4], `is`(closeTo(WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0))))
        assertThat(n.weightedControlPoints[5], `is`(closeTo(WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0))))
        assertThat(n.controlPoints.size, `is`(6))
        assertThat(n.controlPoints[0], `is`(closeTo(Point.xyr(200.0, 300.0, 10.0))))
        assertThat(n.controlPoints[1], `is`(closeTo(Point.xyr(200.0, 100.0, 20.0))))
        assertThat(n.controlPoints[2], `is`(closeTo(Point.xyr(400.0, 100.0, 30.0))))
        assertThat(n.controlPoints[3], `is`(closeTo(Point.xyr(400.0, 500.0, 30.0))))
        assertThat(n.controlPoints[4], `is`(closeTo(Point.xyr(200.0, 500.0, 20.0))))
        assertThat(n.controlPoints[5], `is`(closeTo(Point.xyr(200.0, 300.0, 10.0))))
        assertThat(n.controlPoints.size, `is`(6))
        assertThat(n.weights[0], `is`(closeTo(1.0)))
        assertThat(n.weights[1], `is`(closeTo(1 / 9.0)))
        assertThat(n.weights[2], `is`(closeTo(1 / 27.0)))
        assertThat(n.weights[3], `is`(closeTo(1 / 27.0)))
        assertThat(n.weights[4], `is`(closeTo(1 / 9.0)))
        assertThat(n.weights[5], `is`(closeTo(1.0)))
        assertThat(n.knotVector, `is`(closeTo(listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))))
        assertThat(n.degree, `is`(3))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(n(0.0), `is`(closeTo(Point.xyr(200.0, 300.0, 10.0))))
        assertThat(n(0.5), `is`(closeTo(Point.xyr(220.0, 240.0, 228 / 16.0))))
        assertThat(n(1.0), `is`(closeTo(Point.xyr(400.0, 300.0, 30.0))))
        assertThat(n(1.5), `is`(closeTo(Point.xyr(220.0, 360.0, 228 / 16.0))))
        assertThat(n(2.0), `is`(closeTo(Point.xyr(200.0, 300.0, 10.0))))
    }

    @Test
    fun testDifferentiate() {
        println("GetDerivative")
        val v0 = Vector(0.0, -200 / 3.0)
        val v1 = Vector(144.0, -192.0)
        val v2 = Vector(0.0, 600.0)
        val v3 = Vector(-144.0, -192.0)
        val v4 = Vector(0.0, -200 / 3.0)

        val d = n.differentiate()

        assertThat(d(0.0), `is`(closeTo(v0)))
        assertThat(d(0.5), `is`(closeTo(v1)))
        assertThat(d(1.0), `is`(closeTo(v2)))
        assertThat(d(1.5), `is`(closeTo(v3)))
        assertThat(d(2.0), `is`(closeTo(v4)))
    }

    @Test
    fun testAffineTransform() {
        println("AffineTransform")
        val t = UniformlyScale(2.0)
            .andThen(Rotate(Vector(0.0, 0.0, 1.0), -FastMath.PI / 2))
            .andThen(Translate(Vector(-200.0, 400.0)))
        val a = n.affineTransform(t)
        val e = Nurbs(
            listOf(
                WeightedPoint(Point.xy(400.0, 0.0), 1.0),
                WeightedPoint(Point.xy(0.0, 0.0), 1 / 9.0),
                WeightedPoint(Point.xy(0.0, -400.0), 1 / 27.0),
                WeightedPoint(Point.xy(800.0, -400.0), 1 / 27.0),
                WeightedPoint(Point.xy(800.0, 0.0), 1 / 9.0),
                WeightedPoint(Point.xy(400.0, 0.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testSimilarlyTransform() {
        println("SimilarlyTransform")
        val t = UniformlyScale(2.0).asSimilarity()
            .andThen(Rotate(Vector(0.0, 0.0, 1.0), -FastMath.PI / 2).asSimilarity())
            .andThen(Translate(Vector(-200.0, 400.0)).asSimilarity())
        val a = n.similarlyTransform(t)
        val e = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(400.0, 0.0, 20.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 40.0), 1 / 9.0),
                WeightedPoint(Point.xyr(0.0, -400.0, 60.0), 1 / 27.0),
                WeightedPoint(Point.xyr(800.0, -400.0, 60.0), 1 / 27.0),
                WeightedPoint(Point.xyr(800.0, 0.0, 40.0), 1 / 9.0),
                WeightedPoint(Point.xyr(400.0, 0.0, 20.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val e = Nurbs(
            listOf(
                WeightedPoint(Point.xy(200.0, 300.0), 1.0),
                WeightedPoint(Point.xy(200.0, 100.0), 1 / 9.0),
                WeightedPoint(Point.xy(400.0, 100.0), 1 / 27.0),
                WeightedPoint(Point.xy(400.0, 500.0), 1 / 27.0),
                WeightedPoint(Point.xy(200.0, 500.0), 1 / 9.0),
                WeightedPoint(Point.xy(200.0, 300.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(n.toCrisp(), `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e0 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0)
            ),
            KnotVector.of(3, listOf(0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0))
        )
        assertThat(n.restrict(0.5, 1.0), `is`(closeTo(e0)))
        assertThat(n.restrict(Interval(0.5, 1.0)), `is`(closeTo(e0)))

        val e1 = n
        assertThat(n.restrict(0.0, 2.0), `is`(closeTo(e1)))
        assertThat(n.restrict(Interval(0.0, 2.0)), `is`(closeTo(e1)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val e = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(n.reverse(), `is`(closeTo(e)))
    }

    @Test
    fun testToRationalBeziers() {
        println("ToRationalBeziers")
        val qs = n.toRationalBeziers()
        val e0 = RationalBezier(
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0)
        )
        val e1 = RationalBezier(
            WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
        )
        assertThat(qs.size, `is`(2))
        assertThat(qs[0], `is`(closeTo(e0)))
        assertThat(qs[1], `is`(closeTo(e1)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a00, a01) = n.subdivide(1.0)
        val e00 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0)
            ),
            KnotVector.clamped(Interval(0.0, 1.0), 3, 8)
        )
        val e01 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.clamped(Interval(1.0, 2.0), 3, 8)
        )
        assertThat(a00, `is`(closeTo(e00)))
        assertThat(a01, `is`(closeTo(e01)))

        val (a10, a11) = n.subdivide(0.5)
        val e10 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(3500 / 17.0, 4400 / 17.0, 210 / 17.0), 17 / 54.0),
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 0.5, 0.5, 0.5, 0.5))
        )
        val e11 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(a10, `is`(closeTo(e10)))
        assertThat(a11, `is`(closeTo(e11)))

        val (a20, a21) = n.subdivide(0.0)
        val e20 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        )
        val e21 = n
        assertThat(a20, `is`(closeTo(e20)))
        assertThat(a21, `is`(closeTo(e21)))


        val (a30, a31) = n.subdivide(2.0)
        val e30 = n
        val e31 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(a30, `is`(closeTo(e30)))
        assertThat(a31, `is`(closeTo(e31)))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val e0 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(250.0, 100.0, 22.5), 2 / 27.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(n.insertKnot(0.5), `is`(closeTo(e0)))

        val e1 = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(3500 / 17.0, 4400 / 17.0, 210 / 17.0), 17 / 54.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)
            ),
            KnotVector.of(3, listOf(0.0, 0.0, 0.0, 0.0, 0.5, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        )
        assertThat(n.insertKnot(0.5, 2), `is`(closeTo(e1)))
    }

    @Test
    fun testClose() {
        println("Close")
        val a = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)
            ),
            KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
        ).close()
        val e = Nurbs(
            listOf(
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0)
            ),
            KnotVector.clamped(Interval(0.0, 2.0), 3, 9)
        )

        assertThat(a, `is`(closeTo(e)))
    }
}

