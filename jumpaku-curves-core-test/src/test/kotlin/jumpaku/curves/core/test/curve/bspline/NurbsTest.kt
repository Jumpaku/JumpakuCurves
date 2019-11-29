package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.json.parseJson
import jumpaku.commons.test.math.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.curve.bspline.Nurbs
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class NurbsTest {

    val n = Nurbs(listOf(
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
            WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
            WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
            KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))

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
        assertThat(n.knotVector, `is`(closeTo(KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))))
        assertThat(n.degree, `is`(3))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = n.toString().parseJson().tryMap { Nurbs.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(n)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
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

        assertThat(n.differentiate(0.0), `is`(closeTo(v0)))
        assertThat(n.differentiate(0.5), `is`(closeTo(v1)))
        assertThat(n.differentiate(1.0), `is`(closeTo(v2)))
        assertThat(n.differentiate(1.5), `is`(closeTo(v3)))
        assertThat(n.differentiate(2.0), `is`(closeTo(v4)))

        assertThat(n.derivative(0.0), `is`(closeTo(v0)))
        assertThat(n.derivative(0.5), `is`(closeTo(v1)))
        assertThat(n.derivative(1.0), `is`(closeTo(v2)))
        assertThat(n.derivative(1.5), `is`(closeTo(v3)))
        assertThat(n.derivative(2.0), `is`(closeTo(v4)))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = n.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), -FastMath.PI / 2))
                .andThen(Translate(Vector(-200.0, 400.0))))
        val e = Nurbs(listOf(
                WeightedPoint(Point.xy(400.0, 0.0), 1.0),
                WeightedPoint(Point.xy(0.0, 0.0), 1 / 9.0),
                WeightedPoint(Point.xy(0.0, -400.0), 1 / 27.0),
                WeightedPoint(Point.xy(800.0, -400.0), 1 / 27.0),
                WeightedPoint(Point.xy(800.0, 0.0), 1 / 9.0),
                WeightedPoint(Point.xy(400.0, 0.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val e = Nurbs(listOf(
                WeightedPoint(Point.xy(200.0, 300.0), 1.0),
                WeightedPoint(Point.xy(200.0, 100.0), 1 / 9.0),
                WeightedPoint(Point.xy(400.0, 100.0), 1 / 27.0),
                WeightedPoint(Point.xy(400.0, 500.0), 1 / 27.0),
                WeightedPoint(Point.xy(200.0, 500.0), 1 / 9.0),
                WeightedPoint(Point.xy(200.0, 300.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        assertThat(n.toCrisp(), `is`(closeTo(e)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e = Nurbs(listOf(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0)),
                KnotVector(3, Knot(0.5, 4), Knot(1.0, 4)))
        assertThat(n.restrict(0.5, 1.0), `is`(closeTo(e)))
        assertThat(n.restrict(Interval(0.5, 1.0)), `is`(closeTo(e)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val e = Nurbs(listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
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
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0))
        val e1 = RationalBezier(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0))
        assertThat(qs.size, `is`(2))
        assertThat(qs[0], `is`(closeTo(e0)))
        assertThat(qs[1], `is`(closeTo(e1)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a00, a01) = n.subdivide(1.0)
        val e00 = Nurbs(listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 4)))
        val e01 = Nurbs(listOf(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(1.0, 4), Knot(2.0, 4)))
        assertThat(a00.orThrow(), `is`(closeTo(e00)))
        assertThat(a01.orThrow(), `is`(closeTo(e01)))

        val (a10, a11) = n.subdivide(0.5)
        val e10 = Nurbs(listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(3500 / 17.0, 4400 / 17.0, 210 / 17.0), 17 / 54.0),
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5, 4)))
        val e11 = Nurbs(listOf(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5 / 27.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.5, 4), Knot(1.0, 2), Knot(2.0, 4)))
        assertThat(a10.orThrow(), `is`(closeTo(e10)))
        assertThat(a11.orThrow(), `is`(closeTo(e11)))

        val (a20, a21) = n.subdivide(0.0)
        val e21 = n
        assertThat(a20.isDefined, `is`(false))
        assertThat(a21.orThrow(), `is`(closeTo(e21)))


        val (a30, a31) = n.subdivide(2.0)
        val e30 = n
        assertThat(a30.orThrow(), `is`(closeTo(e30)))
        assertThat(a31.isDefined, `is`(false))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val e0 = Nurbs(listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(250.0, 100.0, 22.5), 2 / 27.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5), Knot(1.0, 2), Knot(2.0, 4)))
        assertThat(n.insertKnot(0.5), `is`(closeTo(e0)))

        val e1 = Nurbs(listOf(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5 / 9.0),
                WeightedPoint(Point.xyr(3500 / 17.0, 4400 / 17.0, 210 / 17.0), 17 / 54.0),
                WeightedPoint(Point.xyr(300.0, 400 / 3.0, 25.0), 1 / 18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1 / 27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1 / 9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5, 2), Knot(1.0, 2), Knot(2.0, 4)))
        assertThat(n.insertKnot(0.5, 2), `is`(closeTo(e1)))
    }

    @Test
    fun testRemoveKnot_Int_Int() {
        println("RemoveKnot")
        val n0 = Nurbs(listOf(
                WeightedPoint(Point.xy(200.0, 300.0), 1.0),
                WeightedPoint(Point.xy(200.0, 280.0), 5 / 9.0),
                WeightedPoint(Point.xy(250.0, 100.0), 2 / 27.0),
                WeightedPoint(Point.xy(400.0, 200.0), 1 / 27.0),
                WeightedPoint(Point.xy(400.0, 500.0), 1 / 27.0),
                WeightedPoint(Point.xy(200.0, 500.0), 1 / 9.0),
                WeightedPoint(Point.xy(200.0, 300.0), 1.0)),
                KnotVector(3,
                        Knot(0.0, 4), Knot(0.5), Knot(1.0, 2), Knot(2.0, 4)))
        val e0 = n.toCrisp()
        assertThat(n0.removeKnot(1, 1), `is`(closeTo(e0)))

        val n1 = Nurbs(listOf(
                WeightedPoint(Point.xy(200.0, 300.0), 1.0),
                WeightedPoint(Point.xy(200.0, 280.0), 5 / 9.0),
                WeightedPoint(Point.xy(3500 / 17.0, 4400 / 17.0), 17 / 54.0),
                WeightedPoint(Point.xy(300.0, 400 / 3.0), 1 / 18.0),
                WeightedPoint(Point.xy(400.0, 200.0), 1 / 27.0),
                WeightedPoint(Point.xy(400.0, 500.0), 1 / 27.0),
                WeightedPoint(Point.xy(200.0, 500.0), 1 / 9.0),
                WeightedPoint(Point.xy(200.0, 300.0), 1.0)),
                KnotVector(3,
                        Knot(0.0, 4), Knot(0.5, 2), Knot(1.0, 2), Knot(2.0, 4)))
        val e1 = n.toCrisp()
        assertThat(n1.removeKnot(1, 2), `is`(closeTo(e1)))
    }

    @Test
    fun testRemoveKnot_Double_Int() {
        println("RemoveKnot")
        val clamped = n.toCrisp()

        for (times in 0..4) {
            val c = clamped.insertKnot(0.5, times).removeKnot(0.5, times)
            assertThat(c, `is`(closeTo(clamped)))
        }
        for (times in 0..2) {
            val c = clamped.insertKnot(1.0, times).removeKnot(1.0, times)
            assertThat(c, `is`(closeTo(clamped)))
        }
        for (times in 0..4) {
            val c = clamped.insertKnot(1.5, times).removeKnot(1.5, times)
            assertThat(c, `is`(closeTo(clamped)))
        }

        val uniform = Nurbs(listOf(
                WeightedPoint(Point.xy(200.0, 300.0), 1.0),
                WeightedPoint(Point.xy(200.0, 100.0), 1 / 9.0),
                WeightedPoint(Point.xy(400.0, 100.0), 1 / 27.0),
                WeightedPoint(Point.xy(400.0, 500.0), 1 / 27.0),
                WeightedPoint(Point.xy(200.0, 500.0), 1 / 9.0),
                WeightedPoint(Point.xy(200.0, 300.0), 1.0)),
                KnotVector.uniform(Interval(0.0, 2.0),3, 10))

        for (times in 0..3) {
            val u = uniform.insertKnot(0.0, times).removeKnot(0.0, times)
            assertThat(u, `is`(closeTo(uniform)))
        }
        for (times in 0..3) {
            val u = uniform.insertKnot(2.0, times).removeKnot(2.0, times)
            assertThat(u, `is`(closeTo(uniform)))
        }
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = Nurbs(listOf(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .clamp()
        val e = Nurbs(listOf(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        assertThat(c, `is`(closeTo(e)))
    }

    @Test
    fun testClose() {
        println("Close")
        val c = Nurbs(listOf(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .close()
        val e = Nurbs(listOf(
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        assertThat(c, `is`(closeTo(e)))
    }
}