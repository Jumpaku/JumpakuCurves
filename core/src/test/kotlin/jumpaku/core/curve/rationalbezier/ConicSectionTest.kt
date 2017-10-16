package jumpaku.core.curve.rationalbezier

import jumpaku.core.affine.*
import jumpaku.core.json.parseToJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*

import org.junit.Test


class ConicSectionTest {

    private val R2 = FastMath.sqrt(2.0)

    @Test
    fun testProperties() {
        println("Properties")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        pointAssertThat(i.begin).isEqualToPoint(Point.xyr(0.0, 1.0, 1.0))
        pointAssertThat(i.far).isEqualToPoint(Point.xyr(R2/2, R2/2, 2.0))
        pointAssertThat(i.end).isEqualToPoint(Point.xyr(1.0, 0.0, 3.0))
        assertThat(i.weight).isEqualTo(R2/2, withPrecision(1.0e-10))
        assertThat(i.degree).isEqualTo(2)
        assertThat(i.domain.begin).isEqualTo(0.0, withPrecision(1.0e-10))
        assertThat(i.domain.end).isEqualTo(1.0, withPrecision(1.0e-10))
        pointAssertThat(i.representPoints[0]).isEqualToPoint(Point.xyr(0.0, 1.0, 1.0))
        pointAssertThat(i.representPoints[1]).isEqualToPoint(Point.xyr(R2/2, R2/2, 2.0))
        pointAssertThat(i.representPoints[2]).isEqualToPoint(Point.xyr(1.0, 0.0, 3.0))
        rationalBezierAssertThat(i.toCrispRationalBezier()).isEqualToRationalBezier(RationalBezier(
                WeightedPoint(Point.xyr(0.0, 1.0, 0.0), 1.0),
                WeightedPoint(Point.xyr(1.0, 1.0, 0.0), 1 / R2),
                WeightedPoint(Point.xyr(1.0, 0.0, 0.0), 1.0)))
    }

    @Test
    fun testToString() {
        println("ToString")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        conicSectionAssertThat(i.toString().parseToJson().get().conicSection)
                .isEqualConicSection(i)
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        val d = i.derivative

        vectorAssertThat(i.differentiate(0.0)).isEqualToVector(
                Vector(R2, 0.0))
        vectorAssertThat(i.differentiate(0.25)).isEqualToVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        vectorAssertThat(i.differentiate(0.5)).isEqualToVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        vectorAssertThat(i.differentiate(0.75)).isEqualToVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        vectorAssertThat(i.differentiate(1.0)).isEqualToVector(
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
    fun testEvaluate() {
        println("Evaluate")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)

        pointAssertThat(i.evaluate(0.0)).isEqualToPoint(Point.xyr(0.0, 1.0, 1.0))
        pointAssertThat(i.evaluate(0.25)).isEqualToPoint(Point.xyr((3*R2+1)/(3*R2+10), (3*R2+9)/(3*R2+10), (24+6*R2)/(10+3*R2)))
        pointAssertThat(i.evaluate(0.5)).isEqualToPoint(Point.xyr(1/R2, 1/R2, 2.0))
        pointAssertThat(i.evaluate(0.75)).isEqualToPoint(Point.xyr((3*R2+9)/(3*R2+10), (3*R2+1)/(3*R2+10), (32+6*R2)/(10+3*R2)))
        pointAssertThat(i.evaluate(1.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 3.0))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        val a = i.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = ConicSection(Point.xy(-1.0, 1.0), Point.xy(1 - R2, 1 + R2), Point.xy(1.0, 3.0), R2 / 2)
        conicSectionAssertThat(a).isEqualConicSection(e)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
                .reverse()
        conicSectionAssertThat(i).isEqualConicSection(ConicSection(
                Point.xyr(1.0, 0.0, 3.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(0.0, 1.0, 1.0), R2 / 2))
    }

    @Test
    fun testComplement() {
        println("Complement")
        val i = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
                .complement()
        conicSectionAssertThat(i).isEqualConicSection(ConicSection(
                Point.xyr(0.0, 1.0, 1.0), Point.xyr(-R2 / 2, -R2 / 2, 14 + 8 * R2), Point.xyr(1.0, 0.0, 3.0), -R2 / 2))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = ConicSection(
                Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
                .subdivide(0.5)
        conicSectionAssertThat(rs._1()).isEqualConicSection(ConicSection(
                Point.xyr(0.0, 1.0, 1.0),
                Point.xyr((3 * R2 + 1) / (10 + 3 * R2), (3 * R2 + 9) / (10 + 3 * R2), 2.3027176028699587),
                Point.xyr(R2 / 2, R2 / 2, 2.0), Math.sqrt(2 + R2) / 2), 0.1)
        conicSectionAssertThat(rs._2()).isEqualConicSection(ConicSection(
                Point.xyr(R2 / 2, R2 / 2, 2.0),
                Point.xyr((3 * R2 + 9) / (10 + 3 * R2), (3 * R2 + 1) / (10 + 3 * R2), 2.3027176028699587),
                Point.xyr(1.0, 0.0, 3.0), Math.sqrt(2 + R2) / 2), 0.1)
    }

    @Test
    fun testToArcLengthCurve() {
        println("ToArcLengthCurve")
        val l = ConicSection(Point.xy(200.0, 300.0),
                Point.xy(100.0 * (2 - R2 / 2), 100.0 * (2 - R2 / 2)),
                Point.xy(300.0, 200.0),
                -R2 / 2).toArcLengthCurve().arcLength()
        assertThat(l).isEqualTo(Math.PI*150, withPrecision(0.1))
    }
}
