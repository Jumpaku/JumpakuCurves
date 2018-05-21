package jumpaku.core.test.curve.bezier

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bezier.BezierDerivative
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToVector
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class BezierDerivativeTest {

    @Test
    fun testProperties() {
        println("Properties")
        val b4 = BezierDerivative(Vector(1.0, -2.0, 0.0), Vector(2.0, -1.0, 0.0), Vector(0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
        b4.controlVectors[0].shouldEqualToVector(Vector(1.0, -2.0, 0.0))
        b4.controlVectors[1].shouldEqualToVector(Vector(2.0, -1.0, 0.0))
        b4.controlVectors[2].shouldEqualToVector(Vector(0.0, 2.0))
        b4.controlVectors[3].shouldEqualToVector(Vector(2.0, 1.0, 0.0))
        b4.controlVectors[4].shouldEqualToVector(Vector(1.0, 2.0, 0.0))
        b4.controlVectors.size().shouldEqualTo(5)
        b4.degree.shouldEqualTo(4)
        b4.domain.begin.shouldBeCloseTo(0.0)
        b4.domain.end.shouldBeCloseTo(1.0)
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        p.toString().parseJson().flatMap { BezierDerivative.fromJson(it) }.get().toBezier().shouldEqualToBezier(p.toBezier())
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val b4 = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        b4.evaluate(0.0).shouldEqualToVector(Vector(-2.0, 0.0))
        b4.evaluate(0.25).shouldEqualToVector(Vector(-1.0, 27 / 64.0))
        b4.evaluate(0.5).shouldEqualToVector(Vector(0.0, 0.75))
        b4.evaluate(0.75).shouldEqualToVector(Vector(1.0, 27 / 64.0))
        b4.evaluate(1.0).shouldEqualToVector(Vector(2.0, 0.0))
    }

    @Test
    fun testDifferentiate() {
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
        val d = b.derivative
        d.toBezier().shouldEqualToBezier(Bezier(Point.xy(4.0, 0.0), Point.xy(4.0, 8.0), Point.xy(4.0, -8.0), Point.xy(4.0, 0.0)))
        b.differentiate(0.0).shouldEqualToVector(Vector(4.0, 0.0))
        b.differentiate(0.25).shouldEqualToVector(Vector(4.0, 2.25))
        b.differentiate(0.5).shouldEqualToVector(Vector(4.0, 0.0))
        b.differentiate(0.75).shouldEqualToVector(Vector(4.0, -2.25))
        b.differentiate(1.0).shouldEqualToVector(Vector(4.0, 0.0))
        d.evaluate(0.0).shouldEqualToVector(Vector(4.0, 0.0))
        d.evaluate(0.25).shouldEqualToVector(Vector(4.0, 2.25))
        d.evaluate(0.5).shouldEqualToVector(Vector(4.0, 0.0))
        d.evaluate(0.75).shouldEqualToVector(Vector(4.0, -2.25))
        d.evaluate(1.0).shouldEqualToVector(Vector(4.0, 0.0))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val b = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))

        b.restrict(0.25, 0.5).toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-1.0, 27 / 64.0), Point.xy(-3 / 4.0, 9 / 16.0), Point.xy(-1 / 2.0, 11 / 16.0), Point.xy(-1 / 4.0, 3 / 4.0), Point.xy(0.0, 3 / 4.0)))
        b.restrict(Interval(0.25, 0.5)).toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-1.0, 27 / 64.0), Point.xy(-3 / 4.0, 9 / 16.0), Point.xy(-1 / 2.0, 11 / 16.0), Point.xy(-1 / 4.0, 3 / 4.0), Point.xy(0.0, 3 / 4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = BezierDerivative(Vector(-2.0, 0.0), Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0), Vector(2.0, 0.0))
                .reverse()
        r.toBezier().shouldEqualToBezier(Bezier(
                Point.xy(2.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(-1.0, 0.0), Point.xy(-2.0, 0.0)))
    }
    @Test
    fun testElevate() {
        println("Elevate")
        val instance = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0))
                .elevate()
        val expected = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1 / 3.0, 4 / 3.0), Point.xy(1 / 3.0, 4 / 3.0), Point.xy(1.0, 0.0))
        instance.toBezier().shouldEqualToBezier(expected)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val b1 = BezierDerivative(Vector(-1.0, 2.0), Vector(1.0, 1.0))
                .reduce()
        val e1 = Bezier(Point.xy(0.0, 1.5))
        b1.toBezier().shouldEqualToBezier(e1)

        val b2 = BezierDerivative(Vector(-1.0, 0.0), Vector(0.0, 2.0), Vector(1.0, 0.0))
                .reduce()
        val e2 = Bezier(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0))
        b2.toBezier().shouldEqualToBezier(e2)

        val b3 = BezierDerivative(Vector(-1.0, 0.0), Vector(-1 / 3.0, 4 / 3.0), Vector(1 / 3.0, 4 / 3.0), Vector(1.0, 0.0))
                .reduce()
        val e3 = Bezier(Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0))
        b3.toBezier().shouldEqualToBezier(e3)

        val b4 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.5, 1.0), Vector(0.0, 4 / 3.0), Vector(0.5, 1.0), Vector(1.0, 0.0))
                .reduce()
        val e4 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-1 / 3.0, 4 / 3.0), Point.xy(1 / 3.0, 4 / 3.0), Point.xy(1.0, 0.0))
        b4.toBezier().shouldEqualToBezier(e4)

        val b5 = BezierDerivative(Vector(-1.0, 0.0), Vector(-0.6, 0.8), Vector(-0.3, 1.2), Vector(0.3, 1.2), Vector(0.6, 0.8), Vector(1.0, 0.0))
                .reduce()
        val e5 = Bezier(Point.xy(-1.0, 0.0), Point.xy(-0.5, 1.0), Point.xy(0.0, 4 / 3.0), Point.xy(0.5, 1.0), Point.xy(1.0, 0.0))
        b5.toBezier().shouldEqualToBezier(e5)
    }
    @Test
    fun testSubdivide() {
        println("Subdivide")
        val bs = BezierDerivative(Vector(1.0, -2.0, 0.0), Vector(2.0, -1.0, 0.0), Vector(0.0, 0.0, 2.0), Vector(2.0, 1.0, 0.0), Vector(1.0, 2.0, 0.0))
                .subdivide(0.25)
        bs._1().toBezier().shouldEqualToBezier(Bezier(
                Point.xyz(1.0, -2.0, 0.0), Point.xyz(5 / 4.0, -7 / 4.0, 0.0), Point.xyz(21 / 16.0, -3 / 2.0, 1 / 8.0), Point.xyz(83 / 64.0, -5 / 4.0, 9 / 32.0), Point.xyz(322 / 256.0, -1.0, 27 / 64.0)))
        bs._2().toBezier().shouldEqualToBezier(Bezier(
                Point.xyz(322 / 256.0, -1.0, 27 / 64.0), Point.xyz(73 / 64.0, -1 / 4.0, 27 / 32.0), Point.xyz(13 / 16.0, 1 / 2.0, 9 / 8.0), Point.xyz(7 / 4.0, 5 / 4.0, 0.0), Point.xyz(1.0, 2.0, 0.0)))
    }

    @Test
    fun testExtend() {
        println("Extend")
        val extendBack = BezierDerivative(
                Vector(-2.0, 0.0), Vector(-7 / 4.0, 0.0), Vector(-3 / 2.0, 1 / 8.0), Vector(-5 / 4.0, 9 / 32.0), Vector(-1.0, 27 / 64.0))
                .extend(4.0)
        extendBack.toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)))

        val extendFront = BezierDerivative(
                Vector(-1.0, 27 / 64.0), Vector(-1 / 4.0, 27 / 32.0), Vector(1 / 2.0, 9 / 8.0), Vector(5 / 4.0, 0.0), Vector(2.0, 0.0))
                .extend(-1/3.0)
        extendFront.toBezier().shouldEqualToBezier(Bezier(
                Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0)))
    }
}