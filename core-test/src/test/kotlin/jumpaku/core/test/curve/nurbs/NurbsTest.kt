package jumpaku.core.test.curve.nurbs

import io.vavr.collection.Array
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.geom.WeightedPoint
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.curve.Interval
import jumpaku.core.curve.Knot
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.nurbs.Nurbs
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToPoint
import jumpaku.core.test.affine.shouldEqualToVector
import jumpaku.core.test.affine.shouldEqualToWeightedPoint
import jumpaku.core.test.curve.rationalbezier.shouldEqualToRationalBezier
import jumpaku.core.test.curve.shouldEqualToKnotVector
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
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

        n.weightedControlPoints.size().shouldEqualTo(6)
        n.weightedControlPoints[0].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0))
        n.weightedControlPoints[1].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0))
        n.weightedControlPoints[2].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0))
        n.weightedControlPoints[3].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0))
        n.weightedControlPoints[4].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0))
        n.weightedControlPoints[5].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0))

        n.controlPoints.size().shouldEqualTo(6)
        n.controlPoints[0].shouldEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
        n.controlPoints[1].shouldEqualToPoint(Point.xyr( 200.0, 100.0, 20.0))
        n.controlPoints[2].shouldEqualToPoint(Point.xyr( 400.0, 100.0, 30.0))
        n.controlPoints[3].shouldEqualToPoint(Point.xyr( 400.0, 500.0, 30.0))
        n.controlPoints[4].shouldEqualToPoint(Point.xyr( 200.0, 500.0, 20.0))
        n.controlPoints[5].shouldEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))

        n.controlPoints.size().shouldEqualTo(6)
        n.weights[0].shouldBeCloseTo(1.0)
        n.weights[1].shouldBeCloseTo(1/9.0)
        n.weights[2].shouldBeCloseTo(1/27.0)
        n.weights[3].shouldBeCloseTo(1/27.0)
        n.weights[4].shouldBeCloseTo(1/9.0)
        n.weights[5].shouldBeCloseTo(1.0)

        n.knotVector.shouldEqualToKnotVector(KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))

        n.degree.shouldEqualTo(3)
    }

    @Test
    fun testToString() {
        println("ToString")
        n.toString().parseJson().flatMap { Nurbs.fromJson(it) }.get().shouldEqualToNurbs(n)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        n(0.0).shouldEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
        n(0.5).shouldEqualToPoint(Point.xyr(220.0, 240.0, 228/16.0))
        n(1.0).shouldEqualToPoint(Point.xyr( 400.0, 300.0, 30.0))
        n(1.5).shouldEqualToPoint(Point.xyr(220.0, 360.0, 228/16.0))
        n(2.0).shouldEqualToPoint(Point.xyr( 200.0, 300.0, 10.0))
    }

    @Test
    fun testDifferentiate() {
        println("GetDerivative")
        val v0 = Vector(0.0, -200/3.0)
        val v1 = Vector(144.0, -192.0)
        val v2 = Vector(0.0, 600.0)
        val v3 = Vector(-144.0, -192.0)
        val v4 = Vector(0.0, -200/3.0)

        n.differentiate(0.0).shouldEqualToVector(v0)
        n.differentiate(0.5).shouldEqualToVector(v1)
        n.differentiate(1.0).shouldEqualToVector(v2)
        n.differentiate(1.5).shouldEqualToVector(v3)
        n.differentiate(2.0).shouldEqualToVector(v4)

        n.derivative(0.0).shouldEqualToVector(v0)
        n.derivative(0.5).shouldEqualToVector(v1)
        n.derivative(1.0).shouldEqualToVector(v2)
        n.derivative(1.5).shouldEqualToVector(v3)
        n.derivative(2.0).shouldEqualToVector(v4)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = n.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), -FastMath.PI / 2))
                .andThen(Translate(Vector(-200.0, 400.0))))
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xy( 400.0,    0.0), 1.0),
                WeightedPoint(Point.xy(   0.0,    0.0), 1/9.0),
                WeightedPoint(Point.xy(   0.0, -400.0), 1/27.0),
                WeightedPoint(Point.xy( 800.0, -400.0), 1/27.0),
                WeightedPoint(Point.xy( 800.0,    0.0), 1/9.0),
                WeightedPoint(Point.xy( 400.0,    0.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        a.shouldEqualToNurbs(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0),
                WeightedPoint(Point.xy( 200.0, 100.0), 1/9.0),
                WeightedPoint(Point.xy( 400.0, 100.0), 1/27.0),
                WeightedPoint(Point.xy( 400.0, 500.0), 1/27.0),
                WeightedPoint(Point.xy( 200.0, 500.0), 1/9.0),
                WeightedPoint(Point.xy( 200.0, 300.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        n.toCrisp().shouldEqualToNurbs(e)
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
        n.restrict(0.5, 1.0).shouldEqualToNurbs(e)
        n.restrict(Interval(0.5, 1.0)).shouldEqualToNurbs(e)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val e = Nurbs(Array.of(
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0),
                WeightedPoint(Point.xyr( 200.0, 500.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 400.0, 500.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 400.0, 100.0, 30.0), 1/27.0),
                WeightedPoint(Point.xyr( 200.0, 100.0, 20.0), 1/9.0),
                WeightedPoint(Point.xyr( 200.0, 300.0, 10.0), 1.0)),
                KnotVector(3, Knot(0.0, 4), Knot(1.0, 2), Knot(2.0, 4)))
        n.reverse().shouldEqualToNurbs(e)
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
        qs.size().shouldEqualTo(2)
        qs[0].shouldEqualToRationalBezier(e0)
        qs[1].shouldEqualToRationalBezier(e1)
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
        a00.get().shouldEqualToNurbs(e00)
        a01.get().shouldEqualToNurbs(e01)

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
        a10.get().shouldEqualToNurbs(e10)
        a11.get().shouldEqualToNurbs(e11)

        val (a20, a21) = n.subdivide(0.0)
        val e21 = n
        a20.isDefined.shouldBeFalse()
        a21.get().shouldEqualToNurbs(e21)
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
        n.insertKnot(0.5).shouldEqualToNurbs(e0)

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
        n.insertKnot(0.5, 2).shouldEqualToNurbs(e1)
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
        n0.removeKnot(1, 1).shouldEqualToNurbs(e0)

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
        n1.removeKnot(1, 2).shouldEqualToNurbs(e1)
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        n.reparametrizeArcLength().arcLength().shouldBeCloseTo(200*Math.PI, 0.1)
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

        c.shouldEqualToNurbs(e)
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

        c.shouldEqualToNurbs(e)
    }
}