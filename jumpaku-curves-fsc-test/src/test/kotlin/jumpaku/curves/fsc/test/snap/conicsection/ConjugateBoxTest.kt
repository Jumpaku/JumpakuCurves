package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConjugateBoxTest {

    val r2 = FastMath.sqrt(2.0)

    val e = ConicSection(
            Point.xyz(0.0, -r2, 1 - r2 / 2),
            Point.xyz(0.0, 0.0, 2.0),
            Point.xyz(0.0, r2, 1 - r2 / 2),
            -r2 / 2)

    val l = ConicSection.lineSegment(Point.xyz(0.0, 1.0, 1.0), Point.xyz(0.0, -1.0, -1.0))

    @Test
    fun testCreate() {
        println("Create")
        val c0 = ConjugateBox.ofConicSection(e)
        assertThat(c0.topLeft, `is`(closeTo(Point.xyz(0.0, -2.0, 2.0))))
        assertThat(c0.topRight, `is`(closeTo(Point.xyz(0.0, 2.0, 2.0))))
        assertThat(c0.bottomLeft, `is`(closeTo(Point.xyz(0.0, -2.0, 0.0))))
        assertThat(c0.bottomRight, `is`(closeTo(Point.xyz(0.0, 2.0, 0.0))))
        assertThat(c0.top, `is`(closeTo(Point.xyz(0.0, 0.0, 1 + r2))))
        assertThat(c0.bottom, `is`(closeTo(Point.xyz(0.0, 0.0, 1 - r2))))
        assertThat(c0.left, `is`(closeTo(Point.xyz(0.0, -2 * r2, 1.0))))
        assertThat(c0.right, `is`(closeTo(Point.xyz(0.0, 2 * r2, 1.0))))
        assertThat(c0.center, `is`(closeTo(Point.xyz(0.0, 0.0, 1.0))))

        val c1 = ConjugateBox.ofConicSection(e.complement())
        assertThat(c1.topLeft, `is`(closeTo(Point.xyz(0.0, -2.0, 2.0))))
        assertThat(c1.topRight, `is`(closeTo(Point.xyz(0.0, 2.0, 2.0))))
        assertThat(c1.bottomLeft, `is`(closeTo(Point.xyz(0.0, -2.0, 0.0))))
        assertThat(c1.bottomRight, `is`(closeTo(Point.xyz(0.0, 2.0, 0.0))))
        assertThat(c0.top, `is`(closeTo(Point.xyz(0.0, 0.0, 1 + r2))))
        assertThat(c0.bottom, `is`(closeTo(Point.xyz(0.0, 0.0, 1 - r2))))
        assertThat(c0.left, `is`(closeTo(Point.xyz(0.0, -2 * r2, 1.0))))
        assertThat(c0.right, `is`(closeTo(Point.xyz(0.0, 2 * r2, 1.0))))
        assertThat(c0.center, `is`(closeTo(Point.xyz(0.0, 0.0, 1.0))))

        val c2 = ConjugateBox.ofConicSection(l)
        assertThat(c2.topLeft, `is`(closeTo(Point.xyz(0.0, 1.0, 1.0))))
        assertThat(c2.topRight, `is`(closeTo(Point.xyz(0.0, -1.0, -1.0))))
        assertThat(c2.bottomLeft, `is`(closeTo(Point.xyz(0.0, 1.0, 1.0))))
        assertThat(c2.bottomRight, `is`(closeTo(Point.xyz(0.0, -1.0, -1.0))))
        assertThat(c2.top, `is`(closeTo(Point.xyz(0.0, 0.0, 0.0))))
        assertThat(c2.bottom, `is`(closeTo(Point.xyz(0.0, 0.0, 0.0))))
        assertThat(c2.left, `is`(closeTo(Point.xyz(0.0, r2, r2))))
        assertThat(c2.right, `is`(closeTo(Point.xyz(0.0, -r2, -r2))))
        assertThat(c2.center, `is`(closeTo(Point.xyz(0.0, 0.0, 0.0))))
    }
}