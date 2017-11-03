package jumpaku.core.curve.nurbs

import io.vavr.API
import io.vavr.collection.Array
import jumpaku.core.affine.*
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.bezierAssertThat
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.curve.knotVectorAssertThat
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.curve.rationalbezier.rationalBezierAssertThat
import jumpaku.core.json.parseToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

fun nurbsAssertThat(actual: Nurbs): NurbsAssert = NurbsAssert(actual)

class NurbsAssert(actual: Nurbs) : AbstractAssert<NurbsAssert, Nurbs>(actual, NurbsAssert::class.java) {

    fun isEqualToNurbs(expected: Nurbs, eps: Double = 1.0e-10): NurbsAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("controlPoints size").isEqualTo(expected.controlPoints.size())

        actual.weightedControlPoints.zip(expected.weightedControlPoints)
                .forEachIndexed {
                    i, (a, e) -> weightedPointAssertThat(a).`as`("nurbs.weightedControlPoints[%d]", i).isEqualToWeightedPoint(e, eps)
                }

        Assertions.assertThat(actual.knotVector.size()).`as`("knotVector size").isEqualTo(expected.knotVector.size())

        knotVectorAssertThat(actual.knotVector).isEqualToKnotVector(expected.knotVector)

        return this
    }
}


class NurbsTest {

    val n = Nurbs(Array.of(
            WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
            WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0),
            WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0),
            WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
            WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
            WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
            KnotVector(3, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))

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

        knotVectorAssertThat(n.knotVector).isEqualToKnotVector(KnotVector(3, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))

        assertThat(n.degree).isEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        nurbsAssertThat(n.toString().parseToJson().get().nurbs).isEqualToNurbs(n)
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
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        nurbsAssertThat(a).isEqualToNurbs(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0),
                WeightedPoint(Point.xyr(300.0, 400/3.0, 25.0), 1/18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0)),
                KnotVector(3, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
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
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
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
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0))
        val e2 = RationalBezier(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0))
        assertThat(qs.size()).isEqualTo(3)
        rationalBezierAssertThat(qs[0]).isEqualToRationalBezier(e0)
        rationalBezierAssertThat(qs[1]).isEqualToRationalBezier(e1)
        rationalBezierAssertThat(qs[2]).isEqualToRationalBezier(e2)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val a0 = n.subdivide(1.0)
        val e00 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 100.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(400.0, 100.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0)),
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
        val e01 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(400.0, 300.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        nurbsAssertThat(a0._1).isEqualToNurbs(e00)
        nurbsAssertThat(a0._2).isEqualToNurbs(e01)

        val a1 = n.subdivide(0.5)
        val e10 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 280.0, 11.0), 5/9.0),
                WeightedPoint(Point.xyr(3500/17.0, 4400/17.0, 210/17.0), 17/54.0),
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0)),
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 0.5, 0.5, 0.5, 0.5))
        val e11 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(220.0, 240.0, 14.25), 5/27.0),
                WeightedPoint(Point.xyr(300.0, 400/3.0, 25.0), 1/18.0),
                WeightedPoint(Point.xyr(400.0, 200.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr(200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        nurbsAssertThat(a1._1).isEqualToNurbs(e10)
        nurbsAssertThat(a1._2).isEqualToNurbs(e11)

        val a2 = n.subdivide(0.0)
        val e20 = Nurbs(Array.of(
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr(200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        val e21 = n
        println(a2._1)
        nurbsAssertThat(a2._1).isEqualToNurbs(e20)
        nurbsAssertThat(a2._2).isEqualToNurbs(e21)
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
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
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
                KnotVector(3, 0.0, 0.0, 0.0, 0.0, 0.5, 0.5, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0))
        nurbsAssertThat(n.insertKnot(0.5, 2)).isEqualToNurbs(e1)
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        assertThat(n.toArcLengthCurve().arcLength()).isEqualTo(200*Math.PI, withPrecision(0.1))
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
                KnotVector.clampedUniform(3, 9))
                .clamp()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(-1.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)),
                KnotVector.clampedUniform(3, 9))

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
                KnotVector.clampedUniform(3, 9))
                .close()
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(-1.0, 1.0, 1.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 1.0),  1.0),
                WeightedPoint(Point.xyr(0.0, 0.0, 0.0), 1.0)),
                KnotVector.clampedUniform(3, 9))

        nurbsAssertThat(c).isEqualToNurbs(e)
    }
}