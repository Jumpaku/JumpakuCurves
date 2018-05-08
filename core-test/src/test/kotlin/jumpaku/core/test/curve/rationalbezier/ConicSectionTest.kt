package jumpaku.core.test.curve.rationalbezier

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.WeightedPoint
import jumpaku.core.affine.identity
import jumpaku.core.curve.Interval
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldBePoint
import jumpaku.core.test.affine.shouldBeVector
import jumpaku.core.test.shouldBeCloseTo
import org.amshove.kluent.shouldBe
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class ConicSectionTest {

    private val R2 = FastMath.sqrt(2.0)

    private val cs = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)

    @Test
    fun testProperties() {
        println("Properties")
        val i = cs
        i.begin.shouldBePoint(Point.xyr(0.0, 1.0, 1.0))
        i.far.shouldBePoint(Point.xyr(R2/2, R2/2, 2.0))
        i.end.shouldBePoint(Point.xyr(1.0, 0.0, 3.0))
        i.weight.shouldBeCloseTo(R2/2)
        i.degree.shouldBe(2)
        i.domain.begin.shouldBeCloseTo(0.0)
        i.domain.end.shouldBeCloseTo(1.0)
        i.representPoints[0].shouldBePoint(Point.xyr(0.0, 1.0, 1.0))
        i.representPoints[1].shouldBePoint(Point.xyr(R2/2, R2/2, 2.0))
        i.representPoints[2].shouldBePoint(Point.xyr(1.0, 0.0, 3.0))
    }


    @Test
    fun testToCrispQuadratic() {
        println("ToCrispQuadratic")
        val i = cs
        i.toCrispQuadratic().get().shouldBeRationalBezier(RationalBezier(
                WeightedPoint(Point.xy(0.0, 1.0), 1.0),
                WeightedPoint(Point.xy(1.0, 1.0), 1 / R2),
                WeightedPoint(Point.xy(1.0, 0.0), 1.0)))
    }

    @Test
    fun testToString() {
        println("ToString")
        val i = cs
        i.toString().parseJson().flatMap { ConicSection.fromJson(it) }.get().shouldBeConicSection(i)
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val i = cs
        val d = i.derivative

        i.differentiate(0.0).shouldBeVector(
                Vector(R2, 0.0))
        i.differentiate(0.25).shouldBeVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        i.differentiate(0.5).shouldBeVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        i.differentiate(0.75).shouldBeVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        i.differentiate(1.0).shouldBeVector(
                Vector(0.0, -R2))

        d.evaluate(0.0).shouldBeVector(
                Vector(R2, 0.0))
        d.evaluate(0.25).shouldBeVector(
                Vector((40 - 12 * R2) * (6 + 72 * R2) / (41 * 41), (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)))
        d.evaluate(0.5).shouldBeVector(
                Vector(4 - 2 * R2, -4 + 2 * R2))
        d.evaluate(0.75).shouldBeVector(
                Vector(-(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41), -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)))
        d.evaluate(1.0).shouldBeVector(
                Vector(0.0, -R2))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        val i = cs

        i.evaluate(0.0).shouldBePoint(Point.xyr(0.0, 1.0, 1.0))
        i.evaluate(0.25).shouldBePoint(Point.xyr((3*R2+1)/(3*R2+10), (3*R2+9)/(3*R2+10), (24+6*R2)/(10+3*R2)))
        i.evaluate(0.5).shouldBePoint(Point.xyr(1/R2, 1/R2, 2.0))
        i.evaluate(0.75).shouldBePoint(Point.xyr((3*R2+9)/(3*R2+10), (3*R2+1)/(3*R2+10), (32+6*R2)/(10+3*R2)))
        i.evaluate(1.0).shouldBePoint(Point.xyr(1.0, 0.0, 3.0))
    }

    @Test
    fun testTransform() {
        println("Transform")
        val i = cs
        val a = i.transform(identity.andScale(2.0).andRotate(Vector(0.0, 0.0, 1.0), FastMath.PI/2).andTranslate(Vector(1.0, 1.0)))
        val e = ConicSection(Point.xy(-1.0, 1.0), Point.xy(1 - R2, 1 + R2), Point.xy(1.0, 3.0), R2 / 2)
        a.shouldBeConicSection(e)
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val i = cs.toCrisp()
        i.shouldBeConicSection(
                ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val i = cs.reverse()
        i.shouldBeConicSection(ConicSection(
                Point.xyr(1.0, 0.0, 3.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(0.0, 1.0, 1.0), R2 / 2))
    }

    @Test
    fun testComplement() {
        println("Complement")
        val i = cs.complement()
        i.shouldBeConicSection(ConicSection(
                Point.xyr(0.0, 1.0, 1.0), Point.xyr(-R2 / 2, -R2 / 2, 14 + 8 * R2), Point.xyr(1.0, 0.0, 3.0), -R2 / 2))
    }

    @Test
    fun testCenter() {
        println("Center")
        val i = cs.center()
        i.get().shouldBePoint(Point.xyr(0.0, 0.0, 4*R2 + 6))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = ConicSection(
                Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
                .subdivide(0.5)
        rs._1().shouldBeConicSection(ConicSection(
                Point.xyr(0.0, 1.0, 1.0),
                Point.xyr((3 * R2 + 1) / (10 + 3 * R2), (3 * R2 + 9) / (10 + 3 * R2), 2.3027176028699587),
                Point.xyr(R2 / 2, R2 / 2, 2.0), Math.sqrt(2 + R2) / 2), 0.1)
        rs._2().shouldBeConicSection(ConicSection(
                Point.xyr(R2 / 2, R2 / 2, 2.0),
                Point.xyr((3 * R2 + 9) / (10 + 3 * R2), (3 * R2 + 1) / (10 + 3 * R2), 2.3027176028699587),
                Point.xyr(1.0, 0.0, 3.0), Math.sqrt(2 + R2) / 2), 0.1)
    }

    @Test
    fun testRestrict(){
        println("Restrict")
        val r0 = cs.restrict(Interval(0.0, 0.5))
        r0.shouldBeConicSection(ConicSection(
                Point.xyr(0.0, 1.0, 1.0),
                Point.xyr((3 * R2 + 1) / (10 + 3 * R2), (3 * R2 + 9) / (10 + 3 * R2), 2.3027176028699587),
                Point.xyr(R2 / 2, R2 / 2, 2.0), Math.sqrt(2 + R2) / 2), 0.1)
        val r1 = cs.restrict(0.5, 1.0)
        r1.shouldBeConicSection(ConicSection(
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
                -R2 / 2).reparametrizeArcLength().arcLength()
        l.shouldBeCloseTo(Math.PI*150, 0.1)
    }
}
