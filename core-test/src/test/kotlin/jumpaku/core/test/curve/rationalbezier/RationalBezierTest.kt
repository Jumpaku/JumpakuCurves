package jumpaku.core.test.curve.rationalbezier

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.geom.WeightedPoint
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.json.parseJson
import jumpaku.core.test.geom.shouldEqualToPoint
import jumpaku.core.test.geom.shouldEqualToVector
import jumpaku.core.test.geom.shouldEqualToWeightedPoint
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class RationalBezierTest {

    private val R2 = FastMath.sqrt(2.0)

    private val rb = RationalBezier(
            WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
            WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
            WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
            WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

    @Test
    fun testProperties() {
        println("Properties")
        val wp = rb.weightedControlPoints
        wp.size.shouldEqualTo(4)
        wp[0].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0))
        wp[1].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3))
        wp[2].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3))
        wp[3].shouldEqualToWeightedPoint(WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

        val cp = rb.controlPoints
        cp.size.shouldEqualTo(4)
        cp[0].shouldEqualToPoint(Point.xyr( 0.0,  1.0,  1.0))
        cp[1].shouldEqualToPoint(Point.xyr(2-R2,  1.0, 3-R2))
        cp[2].shouldEqualToPoint(Point.xyr( 1.0, 2-R2, 1+R2))
        cp[3].shouldEqualToPoint(Point.xyr(1.0, 0.0, 3.0))

        val w = rb.weights
        w.size.shouldEqualTo(4)
        w[0].shouldBeCloseTo(1.0)
        w[1].shouldBeCloseTo((1+R2)/3)
        w[2].shouldBeCloseTo((1+R2)/3)
        w[3].shouldBeCloseTo(1.0)

        rb.degree.shouldEqualTo(3)

        val i = rb.domain
        i.begin.shouldBeCloseTo(0.0)
        i.end.shouldBeCloseTo(1.0)
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        rb.evaluate(0.0).shouldEqualToPoint(
                Point.xyr(0.0, 1.0, 1.0))
        rb.evaluate(0.25).shouldEqualToPoint(
                Point.xyr((3*R2 + 1)/(3*R2 + 10), (3*R2 + 9)/(3*R2 + 10), (12 + 6*R2)/(10 + 3*R2)))
        rb.evaluate(0.5).shouldEqualToPoint(
                Point.xyr(1/R2, 1/R2, 2.0))
        rb.evaluate(0.75).shouldEqualToPoint(
                Point.xyr((3*R2 + 9)/(3*R2 + 10), (3*R2 + 1)/(3*R2 + 10), (28 + 6*R2)/(10 + 3*R2)))
        rb.evaluate(1.0).shouldEqualToPoint(
                Point.xyr(1.0, 0.0, 3.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val d = rb.derivative

        rb.differentiate(0.0).shouldEqualToVector(
                Vector(R2, 0.0))
        rb.differentiate(0.25).shouldEqualToVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        rb.differentiate(0.5).shouldEqualToVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        rb.differentiate(0.75).shouldEqualToVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        rb.differentiate(1.0).shouldEqualToVector(
                Vector(0.0, -R2))

        d.evaluate(0.0).shouldEqualToVector(
                Vector(R2, 0.0))
        d.evaluate(0.25).shouldEqualToVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        d.evaluate(0.5).shouldEqualToVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        d.evaluate(0.75).shouldEqualToVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        d.evaluate(1.0).shouldEqualToVector(
                Vector(0.0, -R2))
    }

    @Test
    fun testToString() {
        println("ToString")
        rb.toString().parseJson().tryFlatMap { RationalBezier.fromJson(it) }.orThrow().shouldEqualToRationalBezier(rb)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val a = rb.transform(UniformlyScale(2.0)
                .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
                .andThen(Translate(Vector(1.0, 1.0))))
        val e = RationalBezier(
                WeightedPoint(Point.xy(-1.0, 1.0), 1.0),
                WeightedPoint(Point.xy(-1.0, 5 - 2 * R2), (1 + R2) / 3),
                WeightedPoint(Point.xy(2 * R2 - 3, 3.0), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 3.0), 1.0))
        a.shouldEqualToRationalBezier(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val e = RationalBezier(
                WeightedPoint(Point.xy(0.0, 1.0), 1.0),
                WeightedPoint(Point.xy(2 - R2, 1.0), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 2 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 0.0), 1.0))
        rb.toCrisp().shouldEqualToRationalBezier(e)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val r1 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 0.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1.0))
                .restrict(0.25, 0.5)
        r1.shouldEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.19, 0.55, 2.0), 25 / 16.0),
                WeightedPoint(Point.xyr(7.5 / 27, 15.5 / 27, 2.0), 27 / 16.0),
                WeightedPoint(Point.xyr(11 / 28.0, 15 / 28.0, 2.0), 7 / 4.0),
                WeightedPoint(Point.xyr(0.5, 0.5, 2.0), 7 / 4.0)))

        val r2 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 0.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1.0))
                .restrict(Interval(0.25, 0.5))
        r2.shouldEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.19, 0.55, 2.0), 25 / 16.0),
                WeightedPoint(Point.xyr(7.5 / 27, 15.5 / 27, 2.0), 27 / 16.0),
                WeightedPoint(Point.xyr(11 / 28.0, 15 / 28.0, 2.0), 7 / 4.0),
                WeightedPoint(Point.xyr(0.5, 0.5, 2.0), 7 / 4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        rb.reverse().shouldEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0)))
    }

    @Test
    fun testElevate() {
        println("Elevate")
        val r3 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .elevate()
        val e3 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r3.shouldEqualToRationalBezier(e3)

        val r4 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .elevate()
        val e4 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr((3 - R2) / 2, (3 - R2) / 2, 2.0), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r4.shouldEqualToRationalBezier(e4)

        val r5 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr((3 - R2) / 2, (3 - R2) / 2, 2.0), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .elevate()
        val e5 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr((3 * R2 - 2) / 7, 1.0, (5 + 3 * R2) / 7), (3 + R2) / 5),
                WeightedPoint(Point.xyr((14 - 9 * R2) / 2, (6 - 3 * R2) / 2, 6 - 3 * R2), (4 + 3 * R2) / 10),
                WeightedPoint(Point.xyr((6 - 3 * R2) / 2, (14 - 9 * R2) / 2, 3 * R2 - 2), (4 + 3 * R2) / 10),
                WeightedPoint(Point.xyr(1.0, (3 * R2 - 2) / 7, (23 - 3 * R2) / 7), (3 + R2) / 5),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r5.shouldEqualToRationalBezier(e5)
    }

    @Test
    fun testReduce() {
        println("Reduce")
        val r4 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .reduce()
        val e4 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2 + 2 * R2), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r4.shouldEqualToRationalBezier(e4)

        val r5 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr((3 - R2) / 2, (3 - R2) / 2, 2.0), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .reduce()
        val e5 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 7 * R2 - 5), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r5.shouldEqualToRationalBezier(e5)

        val r6 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr((3 * R2 - 2) / 7, 1.0, (5 + 3 * R2) / 7), (3 + R2) / 5),
                WeightedPoint(Point.xyr((14 - 9 * R2) / 2, (6 - 3 * R2) / 2, 6 - 3 * R2), (4 + 3 * R2) / 10),
                WeightedPoint(Point.xyr((6 - 3 * R2) / 2, (14 - 9 * R2) / 2, 3 * R2 - 2), (4 + 3 * R2) / 10),
                WeightedPoint(Point.xyr(1.0, (3 * R2 - 2) / 7, (23 - 3 * R2) / 7), (3 + R2) / 5),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .reduce()
        val e6 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, 2.0), (2 + R2) / 4),
                WeightedPoint(Point.xyr((3 - R2) / 2, (3 - R2) / 2, 4 * R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 10 - 4 * R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        r6.shouldEqualToRationalBezier(e6)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .subdivide(0.5)
        rs._1().shouldEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4)))
        rs._2().shouldEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0)))
    }
}