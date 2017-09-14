package jumpaku.core.curve.rationalbezier

import jumpaku.core.affine.*
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*

import org.junit.Test
import jumpaku.core.curve.Interval
import jumpaku.core.json.parseToJson


class RationalBezierTest {

    private val R2 = FastMath.sqrt(2.0)

    @Test
    fun testProperties() {
        println("Properties")
        val r = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3.0), 1.0))

        val wp = r.weightedControlPoints
        assertThat(wp.size()).isEqualTo(4)
        weightedPointAssertThat(wp[0]).isEqualToWeightedPoint(WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0))
        weightedPointAssertThat(wp[1]).isEqualToWeightedPoint(WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3))
        weightedPointAssertThat(wp[2]).isEqualToWeightedPoint(WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3))
        weightedPointAssertThat(wp[3]).isEqualToWeightedPoint(WeightedPoint(Point.xyr(2 - R2, 1.0, 3.0), 1.0))

        val cp = r.controlPoints
        assertThat(cp.size()).isEqualTo(4)
        pointAssertThat(cp[0]).isEqualToPoint(Point.xyr( 0.0,  1.0,  1.0))
        pointAssertThat(cp[1]).isEqualToPoint(Point.xyr(2-R2,  1.0, 3-R2))
        pointAssertThat(cp[2]).isEqualToPoint(Point.xyr( 1.0, 2-R2, 1+R2))
        pointAssertThat(cp[3]).isEqualToPoint(Point.xyr(2-R2,  1.0,  3.0))

        val w = r.weights
        assertThat(w.size()).isEqualTo(4)
        assertThat(w[0]).isEqualTo(     1.0, withPrecision(1.0e-10))
        assertThat(w[1]).isEqualTo((1+R2)/3, withPrecision(1.0e-10))
        assertThat(w[2]).isEqualTo((1+R2)/3, withPrecision(1.0e-10))
        assertThat(w[3]).isEqualTo(     1.0, withPrecision(1.0e-10))

        val d = r.degree
        assertThat(d).isEqualTo(3)

        val i = r.domain
        assertThat(i.begin).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(i.end  ).isEqualTo(1.0, withPrecision(1.0e-10))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val r = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

        pointAssertThat(r.evaluate(0.0)).isEqualToPoint(
                Point.xyr(0.0, 1.0, 1.0))
        pointAssertThat(r.evaluate(0.25)).isEqualToPoint(
                Point.xyr((3*R2 + 1)/(3*R2 + 10), (3*R2 + 9)/(3*R2 + 10), (12 + 6*R2)/(10 + 3*R2)))
        pointAssertThat(r.evaluate(0.5)).isEqualToPoint(
                Point.xyr(1/R2, 1/R2, 2.0))
        pointAssertThat(r.evaluate(0.75)).isEqualToPoint(
                Point.xyr((3*R2 + 9)/(3*R2 + 10), (3*R2 + 1)/(3*R2 + 10), (28 + 6*R2)/(10 + 3*R2)))
        pointAssertThat(r.evaluate(1.0)).isEqualToPoint(
                Point.xyr(1.0, 0.0, 3.0))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val r = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        val d = r.derivative

        vectorAssertThat(r.differentiate(0.0)).isEqualToVector(
                Vector(R2, 0.0))
        vectorAssertThat(r.differentiate(0.25)).isEqualToVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        vectorAssertThat(r.differentiate(0.5)).isEqualToVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        vectorAssertThat(r.differentiate(0.75)).isEqualToVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        vectorAssertThat(r.differentiate(1.0)).isEqualToVector(
                Vector(0.0, -R2))

        vectorAssertThat(d.evaluate(0.0)).isEqualToVector(
                Vector(R2, 0.0))
        vectorAssertThat(d.evaluate(0.25)).isEqualToVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        vectorAssertThat(d.evaluate(0.5)).isEqualToVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        vectorAssertThat(d.evaluate(0.75)).isEqualToVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        vectorAssertThat(d.evaluate(1.0)).isEqualToVector(
                Vector(0.0, -R2))
    }

    @Test
    fun testToString() {
        println("ToString")
        val p = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

        rationalBezierAssertThat(p.toString().parseToJson().get().rationalBezier).isEqualToRationalBezier(p)
    }

    @Test
    fun testTransform() {
        println("Transform")
        val i = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
        val a = i.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = RationalBezier(
                WeightedPoint(Point.xy(-1.0, 1.0), 1.0),
                WeightedPoint(Point.xy(-1.0, 5 - 2 * R2), (1 + R2) / 3),
                WeightedPoint(Point.xy(2 * R2 - 3, 3.0), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 3.0), 1.0))
        rationalBezierAssertThat(a).isEqualToRationalBezier(e)
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
        rationalBezierAssertThat(r1).isEqualToRationalBezier(RationalBezier(
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
        rationalBezierAssertThat(r2).isEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.19, 0.55, 2.0), 25 / 16.0),
                WeightedPoint(Point.xyr(7.5 / 27, 15.5 / 27, 2.0), 27 / 16.0),
                WeightedPoint(Point.xyr(11 / 28.0, 15 / 28.0, 2.0), 7 / 4.0),
                WeightedPoint(Point.xyr(0.5, 0.5, 2.0), 7 / 4.0)))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val r = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .reverse()

        rationalBezierAssertThat(r).isEqualToRationalBezier(RationalBezier(
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
        rationalBezierAssertThat(r3).isEqualToRationalBezier(e3)

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
        rationalBezierAssertThat(r4).isEqualToRationalBezier(e4)

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
        rationalBezierAssertThat(r5).isEqualToRationalBezier(e5)
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
        rationalBezierAssertThat(r4).isEqualToRationalBezier(e4)

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
        rationalBezierAssertThat(r5).isEqualToRationalBezier(e5)

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
        rationalBezierAssertThat(r6).isEqualToRationalBezier(e6)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .subdivide(0.5)
        rationalBezierAssertThat(rs._1()).isEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4)))
        rationalBezierAssertThat(rs._2()).isEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0)))
    }


    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        val l = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 100.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(100.0, 100.0, 2.0), -1 / R2),
                WeightedPoint(Point.xyr(100.0, 0.0, 3.0), 1.0))
                .toArcLengthCurve().arcLength()
        assertThat(l).isEqualTo(Math.PI*200*0.75, withPrecision(0.1))
    }
}