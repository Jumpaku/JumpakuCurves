package jumpaku.core.test.curve.nurbs

import io.vavr.collection.Array
import jumpaku.core.affine.*
import jumpaku.core.curve.Interval
import jumpaku.core.curve.Knot
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.nurbs.Nurbs
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.test.affine.vectorAssertThat
import jumpaku.core.test.affine.weightedPointAssertThat
import jumpaku.core.test.curve.knotVectorAssertThat
import jumpaku.core.test.curve.rationalbezier.rationalBezierAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.junit.Test


class NurbsTest {

    val n = Nurbs(Array.of(
            WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0),
            WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0),
            WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
            WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
            WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
            KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))

    @Test
    fun testProperties() {
        println("Properties")

        assertThat(n.weightedControlPoints.size()).isEqualTo(6)
        weightedPointAssertThat(n.weightedControlPoints[0]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0))
        weightedPointAssertThat(n.weightedControlPoints[1]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0))
        weightedPointAssertThat(n.weightedControlPoints[2]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0))
        weightedPointAssertThat(n.weightedControlPoints[3]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0))
        weightedPointAssertThat(n.weightedControlPoints[4]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0))
        weightedPointAssertThat(n.weightedControlPoints[5]).isEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0))

        assertThat(n.controlPoints.size()).isEqualTo(6)
        pointAssertThat(n.controlPoints[0]).isEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
        pointAssertThat(n.controlPoints[1]).isEqualToPoint(Point.xyr( 200.0, 100.0, 20.0))
        pointAssertThat(n.controlPoints[2]).isEqualToPoint(Point.xyr( 400.0, 100.0, 30.0))
        pointAssertThat(n.controlPoints[3]).isEqualToPoint(Point.xyr( 400.0, 500.0, 30.0))
        pointAssertThat(n.controlPoints[4]).isEqualToPoint(Point.xyr( 200.0, 500.0, 20.0))
        pointAssertThat(n.controlPoints[5]).isEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))

        assertThat(n.controlPoints.size()).isEqualTo(6)
        assertThat(n.weights[0]).isEqualTo(1.0)
        assertThat(n.weights[1]).isEqualTo(1/9.0)
        assertThat(n.weights[2]).isEqualTo(1/27.0)
        assertThat(n.weights[3]).isEqualTo(1/27.0)
        assertThat(n.weights[4]).isEqualTo(1/9.0)
        assertThat(n.weights[5]).isEqualTo(1.0)

        knotVectorAssertThat(n.knotVector).isEqualToKnotVector(KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))

        assertThat(n.degree).isEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        nurbsAssertThat(n.toString().parseJson().flatMap { Nurbs.fromJson(it) }.get()).isEqualToNurbs(n)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        pointAssertThat(n(0.0)).isEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
        pointAssertThat(n(0.5)).isEqualToPoint(Point.xyr(220.0, 240.0, 228/16.0))
        pointAssertThat(n(1.0)).isEqualToPoint(Point.xyr( 400.0, 300.0, 30.0))
        pointAssertThat(n(1.5)).isEqualToPoint(Point.xyr(220.0, 360.0, 228/16.0))
        pointAssertThat(n(2.0)).isEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
    }

    @Test
    fun testDifferentiate() {
        println("GetDerivative")
        val v0 = Vector(0.0, -200/3.0)
        val v1 = Vector(144.0, -192.0)
        val v2 = Vector(0.0, 600.0)
        val v3 = Vector(-144.0, -192.0)
        val v4 = Vector(0.0, -200/3.0)

        vectorAssertThat(n.differentiate(0.0)).isEqualToVector(v0)
        vectorAssertThat(n.differentiate(0.5)).isEqualToVector(v1)
        vectorAssertThat(n.differentiate(1.0)).isEqualToVector(v2)
        vectorAssertThat(n.differentiate(1.5)).isEqualToVector(v3)
        vectorAssertThat(n.differentiate(2.0)).isEqualToVector(v4)

        vectorAssertThat(n.derivative(0.0)).isEqualToVector(v0)
        vectorAssertThat(n.derivative(0.5)).isEqualToVector(v1)
        vectorAssertThat(n.derivative(1.0)).isEqualToVector(v2)
        vectorAssertThat(n.derivative(1.5)).isEqualToVector(v3)
        vectorAssertThat(n.derivative(2.0)).isEqualToVector(v4)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = n.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), -FastMath.PI/2).andTranslate(Vector(-200.0, 400.0)))
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xy( 400.0,    0.0), 1.0),
                WeightedPoint(Point.xy(   0.0,    0.0), 1/9.0),
                WeightedPoint(Point.xy(   0.0, -400.0), 1/27.0),
                WeightedPoint(Point.xy( 800.0, -400.0), 1/27.0),
                WeightedPoint(Point.xy( 800.0,    0.0), 1/9.0),
                WeightedPoint(Point.xy( 400.0,    0.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(a).isEqualToNurbs(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val a = n.toCrisp()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0),
                WeightedPoint(Point.xy( 200.0, 100.0), 1/9.0),
                WeightedPoint(Point.xy( 400.0, 100.0), 1/27.0),
                WeightedPoint(Point.xy( 400.0, 500.0), 1/27.0),
                WeightedPoint(Point.xy( 200.0, 500.0), 1/9.0),
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(a).isEqualToNurbs(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0),
                WeightedPoint(Point.xyr(300.0, 400/3.0, 25.0), 1/18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0)),
                KnotVector(3, Knot(0.5, 4), Knot(1.0, 4)))
        nurbsAssertThat(n.restrict(0.5, 1.0)).isEqualToNurbs(e)
        nurbsAssertThat(n.restrict(Interval(0.5, 1.0))).isEqualToNurbs(e)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val a = n.reverse()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(a).isEqualToNurbs(e)
    }

    @Test
    fun testToRationalBeziers() {
        println("ToRationalBeziers")
        val qs = n.toRationalBeziers()
        val e0 = RationalBezier(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0))
        val e1 = RationalBezier(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0))
        assertThat(qs.size()).isEqualTo(2)
        rationalBezierAssertThat(qs[0]).isEqualToRationalBezier(e0)
        rationalBezierAssertThat(qs[1]).isEqualToRationalBezier(e1)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a00, a01) = n.subdivide(1.0)
        val e00 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 4)))
        val e01 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(1.0, 4), Knot(2.0, 4)))
        nurbsAssertThat(a00.get()).isEqualToNurbs(e00)
        nurbsAssertThat(a01.get()).isEqualToNurbs(e01)

        val (a10, a11) = n.subdivide(0.5)
        val e10 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5/9.0),
                WeightedPoint(Point.xyr(3500/17.0, 4400/17.0, 210/17.0), 17/54.0),
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5, 4)))
        val e11 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0),
                WeightedPoint(Point.xyr(300.0, 400/3.0, 25.0), 1/18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.5, 4), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(a10.get()).isEqualToNurbs(e10)
        nurbsAssertThat(a11.get()).isEqualToNurbs(e11)

        val (a20, a21) = n.subdivide(0.0)
        val e21 = n
        assertThat(a20.isDefined).isFalse()
        nurbsAssertThat(a21.get()).isEqualToNurbs(e21)
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")
        val e0 = Nurbs(Array.of(
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr( 200.0, 280.0, 11.0), 5/9.0),
                WeightedPoint(Point.xyr( 250.0, 100.0, 22.5), 2/27.0),
                WeightedPoint(Point.xyr( 400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(n.insertKnot(0.5)).isEqualToNurbs(e0)

        val e1 = Nurbs(Array.of(
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr( 200.0, 280.0, 11.0), 5/9.0),
                WeightedPoint(Point.xyr(3500/17.0, 4400/17.0, 210/17.0), 17/54.0),
                WeightedPoint(Point.xyr( 300.0, 400/3.0, 25.0), 1/18.0),
                WeightedPoint(Point.xyr( 400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(0.5, 2), Knot(1.0, 2), Knot(2.0, 4)))
        nurbsAssertThat(n.insertKnot(0.5, 2)).isEqualToNurbs(e1)
    }

    @Test
    fun testRemoveKnot() {
        println("RemoveKnot")
        val n0 = Nurbs(Array.of(
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0),
                WeightedPoint(Point.xy( 200.0, 280.0), 5/9.0),
                WeightedPoint(Point.xy( 250.0, 100.0), 2/27.0),
                WeightedPoint(Point.xy( 400.0, 200.0), 1/27.0),
                WeightedPoint(Point.xy( 400.0, 500.0), 1/27.0),
                WeightedPoint(Point.xy( 200.0, 500.0), 1/9.0),
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0)),
                KnotVector(3,
                        Knot(0.0, 4), Knot(0.5), Knot(1.0, 2), Knot(2.0, 4)))
        val e0 = n.toCrisp()
        nurbsAssertThat(n0.removeKnot(1, 1)).isEqualToNurbs(e0)

        val n1 = Nurbs(Array.of(
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0),
                WeightedPoint(Point.xy( 200.0, 280.0), 5/9.0),
                WeightedPoint(Point.xy(3500/17.0, 4400/17.0), 17/54.0),
                WeightedPoint(Point.xy( 300.0, 400/3.0), 1/18.0),
                WeightedPoint(Point.xy( 400.0, 200.0), 1/27.0),
                WeightedPoint(Point.xy( 400.0, 500.0), 1/27.0),
                WeightedPoint(Point.xy( 200.0, 500.0), 1/9.0),
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0)),
                KnotVector(3,
                        Knot(0.0, 4), Knot(0.5, 2), Knot(1.0, 2), Knot(2.0, 4)))
        val e1 = n.toCrisp()
        nurbsAssertThat(n1.removeKnot(1, 2)).isEqualToNurbs(e1)
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        assertThat(n.reparametrizeArcLength().arcLength()).isEqualTo(200*Math.PI, withPrecision(0.1))
    }

    @Test
    fun testClamp() {
        println("Clamp")
        val c = Nurbs(Array.of(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .clamp()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        nurbsAssertThat(c).isEqualToNurbs(e)
    }

    @Test
    fun testClose() {
        println("Close")
        val c = Nurbs(Array.of(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))
                .close()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0)),
                KnotVector.clamped(Interval(0.0, 2.0), 3, 9))

        nurbsAssertThat(c).isEqualToNurbs(e)
    }
}