package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.core.transform.asSimilarity
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConicSectionTest {

    private val R2 = FastMath.sqrt(2.0)

    private val cs =
        ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)

    @Test
    fun testProperties() {
        println("Properties")
        val i = cs
        assertThat(i.begin, `is`(closeTo(Point.xyr(0.0, 1.0, 1.0))))
        assertThat(i.far, `is`(closeTo(Point.xyr(R2 / 2, R2 / 2, 2.0))))
        assertThat(i.end, `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))
        assertThat(i.weight, `is`(closeTo(R2 / 2)))
        assertThat(i.degree, `is`(2))
        assertThat(i.domain.begin, `is`(closeTo(0.0)))
        assertThat(i.domain.end, `is`(closeTo(1.0)))
        assertThat(i.representPoints[0], `is`(closeTo(Point.xyr(0.0, 1.0, 1.0))))
        assertThat(i.representPoints[1], `is`(closeTo(Point.xyr(R2 / 2, R2 / 2, 2.0))))
        assertThat(i.representPoints[2], `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))
    }


    @Test
    fun testToCrispQuadratic() {
        println("ToCrispQuadratic")
        val i = cs
        assertThat(
            i.toCrispQuadratic().orThrow(), `is`(
                closeTo(
                    RationalBezier(
                        WeightedPoint(Point.xy(0.0, 1.0), 1.0),
                        WeightedPoint(Point.xy(1.0, 1.0), 1 / R2),
                        WeightedPoint(Point.xy(1.0, 0.0), 1.0)
                    )
                )
            )
        )
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val i = cs
        val d = i.differentiate()

        assertThat(d.invoke(0.0), `is`(closeTo(Vector(R2, 0.0))))
        assertThat(
            d.invoke(0.25),
            `is`(
                closeTo(
                    Vector(
                        (40 - 12 * R2) * (6 + 72 * R2) / (41 * 41),
                        (40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41)
                    )
                )
            )
        )
        assertThat(d.invoke(0.5), `is`(closeTo(Vector(4 - 2 * R2, -4 + 2 * R2))))
        assertThat(
            d.invoke(0.75),
            `is`(
                closeTo(
                    Vector(
                        -(40 - 12 * R2) * (-54 + 8 * R2) / (41 * 41),
                        -(40 - 12 * R2) * (6 + 72 * R2) / (41 * 41)
                    )
                )
            )
        )
        assertThat(d.invoke(1.0), `is`(closeTo(Vector(0.0, -R2))))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        val i = cs

        assertThat(i.invoke(1.0), `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))
    }

    @Test
    fun testAffineTransform() {
        println("AffineTransform")
        val i = cs
        val t = UniformlyScale(2.0)
            .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
            .andThen(Translate(Vector(1.0, 1.0)))
        val a = i.affineTransform(t)
        val e = ConicSection(Point.xy(-1.0, 1.0), Point.xy(1 - R2, 1 + R2), Point.xy(1.0, 3.0), R2 / 2)
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testSimilarlyTransform() {
        println("SimilarlyTransform")
        val i = cs
        val t = UniformlyScale(2.0).asSimilarity()
            .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2).asSimilarity())
            .andThen(Translate(Vector(1.0, 1.0)).asSimilarity())
        val a = i.similarlyTransform(t)
        val e =
            ConicSection(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1 - R2, 1 + R2, 4.0), Point.xyr(1.0, 3.0, 6.0), R2 / 2)
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testToCrisp() {
        println("ToCrisp")
        val i = cs.toCrisp()
        assertThat(
            i, `is`(
                closeTo(
                    ConicSection(Point.xy(0.0, 1.0), Point.xy(R2 / 2, R2 / 2), Point.xy(1.0, 0.0), R2 / 2)
                )
            )
        )
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val i = cs.reverse()
        assertThat(
            i, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(1.0, 0.0, 3.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(0.0, 1.0, 1.0), R2 / 2
                    )
                )
            )
        )
    }

    @Test
    fun testComplement() {
        println("Complement")
        val i = cs.complement()
        assertThat(
            i, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(0.0, 1.0, 1.0),
                        Point.xyr(-R2 / 2, -R2 / 2, 14 + 8 * R2),
                        Point.xyr(1.0, 0.0, 3.0),
                        -R2 / 2
                    )
                )
            )
        )
    }

    @Test
    fun testCenter() {
        println("Center")
        val i = cs.center()
        assertThat(i.orThrow(), `is`(closeTo(Point.xyr(0.0, 0.0, 4 * R2 + 6))))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val rs = ConicSection(
            Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2
        )
            .subdivide(0.5)
        assertThat(
            rs.first, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(0.0, 1.0, 1.0),
                        Point.xyr((3 * R2 + 1) / (10 + 3 * R2), (3 * R2 + 9) / (10 + 3 * R2), 2.3027176028699587),
                        Point.xyr(R2 / 2, R2 / 2, 2.0), Math.sqrt(2 + R2) / 2
                    ), 0.1
                )
            )
        )
        assertThat(
            rs.second, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(R2 / 2, R2 / 2, 2.0),
                        Point.xyr((3 * R2 + 9) / (10 + 3 * R2), (3 * R2 + 1) / (10 + 3 * R2), 2.3027176028699587),
                        Point.xyr(1.0, 0.0, 3.0), Math.sqrt(2 + R2) / 2
                    ), 0.1
                )
            )
        )
    }

    @Test
    fun testClipout() {
        println("Clipout")
        val r0 = cs.clipout(Interval(0.0, 0.5))
        assertThat(
            r0, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(0.0, 1.0, 1.0),
                        Point.xyr((3 * R2 + 1) / (10 + 3 * R2), (3 * R2 + 9) / (10 + 3 * R2), 2.3027176028699587),
                        Point.xyr(R2 / 2, R2 / 2, 2.0), Math.sqrt(2 + R2) / 2
                    ), 0.1
                )
            )
        )
        val r1 = cs.clipout(0.5, 1.0)
        assertThat(
            r1, `is`(
                closeTo(
                    ConicSection(
                        Point.xyr(R2 / 2, R2 / 2, 2.0),
                        Point.xyr((3 * R2 + 9) / (10 + 3 * R2), (3 * R2 + 1) / (10 + 3 * R2), 2.3027176028699587),
                        Point.xyr(1.0, 0.0, 3.0), Math.sqrt(2 + R2) / 2
                    ), 0.1
                )
            )
        )
    }
}

