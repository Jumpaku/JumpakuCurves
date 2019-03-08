package jumpaku.curves.core.test.curve.rationalbezier

import jumpaku.commons.json.parseJson
import jumpaku.commons.test.math.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.rationalbezier.RationalBezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
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
        assertThat(wp.size, `is`(4))
        assertThat(wp[0], `is`(closeTo(WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0))))
        assertThat(wp[1], `is`(closeTo(WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3))))
        assertThat(wp[2], `is`(closeTo(WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3))))
        assertThat(wp[3], `is`(closeTo(WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))))

        val cp = rb.controlPoints
        assertThat(cp.size, `is`(4))
        assertThat(cp[0], `is`(closeTo(Point.xyr( 0.0,  1.0,  1.0))))
        assertThat(cp[1], `is`(closeTo(Point.xyr(2-R2,  1.0, 3-R2))))
        assertThat(cp[2], `is`(closeTo(Point.xyr( 1.0, 2-R2, 1+R2))))
        assertThat(cp[3], `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))

        val w = rb.weights
        assertThat(w.size, `is`(4))
        assertThat(w[0], `is`(closeTo(1.0)))
        assertThat(w[1], `is`(closeTo((1+R2)/3)))
        assertThat(w[2], `is`(closeTo((1+R2)/3)))
        assertThat(w[3], `is`(closeTo(1.0)))

        assertThat(rb.degree, `is`(3))

        val i = rb.domain
        assertThat(i.begin, `is`(closeTo(0.0)))
        assertThat(i.end, `is`(closeTo(1.0)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(rb.evaluate(0.0), `is`(closeTo(Point.xyr(0.0, 1.0, 1.0))))
        assertThat(rb.evaluate(0.25), `is`(closeTo(Point.xyr((3*R2 + 1)/(3*R2 + 10), (3*R2 + 9)/(3*R2 + 10), (12 + 6*R2)/(10 + 3*R2)))))
        assertThat(rb.evaluate(0.5), `is`(closeTo(Point.xyr(1/R2, 1/R2, 2.0))))
        assertThat(rb.evaluate(0.75), `is`(closeTo(Point.xyr((3*R2 + 9)/(3*R2 + 10), (3*R2 + 1)/(3*R2 + 10), (28 + 6*R2)/(10 + 3*R2)))))
        assertThat(rb.evaluate(1.0), `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val d = rb.derivative

        assertThat(rb.differentiate(0.0), `is`(closeTo(Vector(R2, 0.0))))
        assertThat(rb.differentiate(0.25), `is`(closeTo(Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))))
        assertThat(rb.differentiate(0.5), `is`(closeTo(Vector(4 - 2 * R2, -4 + 2 * R2))))
        assertThat(rb.differentiate(0.75), `is`(closeTo(Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))))
        assertThat(rb.differentiate(1.0), `is`(closeTo(Vector(0.0, -R2))))

        assertThat(d.evaluate(0.0), `is`(closeTo(Vector(R2, 0.0))))
        assertThat(d.evaluate(0.25), `is`(closeTo(Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))))
        assertThat(d.evaluate(0.5), `is`(closeTo(Vector(4 - 2 * R2, -4 + 2 * R2))))
        assertThat(d.evaluate(0.75), `is`(closeTo(Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))))
        assertThat(d.evaluate(1.0), `is`(closeTo(Vector(0.0, -R2))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = rb.toString().parseJson().tryMap { RationalBezier.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(rb)))
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
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val e = RationalBezier(
                WeightedPoint(Point.xy(0.0, 1.0), 1.0),
                WeightedPoint(Point.xy(2 - R2, 1.0), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 2 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xy(1.0, 0.0), 1.0))
        assertThat(rb.toCrisp(), `is`(closeTo(e)))
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
        assertThat(r1, `is`(closeTo(RationalBezier(
                WeightedPoint(Point.xyr(0.19, 0.55, 2.0), 25 / 16.0),
                WeightedPoint(Point.xyr(7.5 / 27, 15.5 / 27, 2.0), 27 / 16.0),
                WeightedPoint(Point.xyr(11 / 28.0, 15 / 28.0, 2.0), 7 / 4.0),
                WeightedPoint(Point.xyr(0.5, 0.5, 2.0), 7 / 4.0)))))

        val r2 = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 0.0, 2.0), 1.0),
                WeightedPoint(Point.xyr(0.0, 1.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 0.0, 2.0), 2.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1.0))
                .restrict(Interval(0.25, 0.5))
        assertThat(r2, `is`(closeTo(RationalBezier(
                WeightedPoint(Point.xyr(0.19, 0.55, 2.0), 25 / 16.0),
                WeightedPoint(Point.xyr(7.5 / 27, 15.5 / 27, 2.0), 27 / 16.0),
                WeightedPoint(Point.xyr(11 / 28.0, 15 / 28.0, 2.0), 7 / 4.0),
                WeightedPoint(Point.xyr(0.5, 0.5, 2.0), 7 / 4.0)))))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        assertThat(rb.reverse(), `is`(closeTo(RationalBezier(
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 2 - R2, 1 + R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(2 - R2, 1.0, 3 - R2), (1 + R2) / 3),
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0)))))
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
        assertThat(r3, `is`(closeTo(e3)))

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
        assertThat(r4, `is`(closeTo(e4)))

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
        assertThat(r5, `is`(closeTo(e5)))
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
        assertThat(r4, `is`(closeTo(e4)))

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
        assertThat(r5, `is`(closeTo(e5)))

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
        assertThat(r6, `is`(closeTo(e6)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 2.0), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))
                .subdivide(0.5)
        assertThat(rs._1(), `is`(closeTo(RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 1.0), 1.0),
                WeightedPoint(Point.xyr(R2 - 1, 1.0, R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4)))))
        assertThat(rs._2(), `is`(closeTo(RationalBezier(
                WeightedPoint(Point.xyr(R2 / 2, R2 / 2, 2.0), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, R2 - 1, 4 - R2), (2 + R2) / 4),
                WeightedPoint(Point.xyr(1.0, 0.0, 3.0), 1.0)))))
    }
}