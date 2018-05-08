package jumpaku.fsc.oldtest.snap.conicsection

import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.testold.affine.pointAssertThat
import jumpaku.fsc.snap.conicsection.ConjugateBox
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class ConjugateBoxTest {

    val r2 = FastMath.sqrt(2.0)

    val e = ConicSection(
            Point.xyz(0.0, -r2, 1 - r2/2),
            Point.xyz(0.0, 0.0, 2.0),
            Point.xyz(0.0, r2, 1 - r2/2),
            -r2/2)

    val l = ConicSection.lineSegment(Point.xyz(0.0, 1.0, 1.0), Point.xyz(0.0, -1.0, -1.0))

    @Test
    fun testCreate() {
        println("Create")
        val c0 = ConjugateBox.ofConicSection(e)
        pointAssertThat(c0.topLeft).isEqualToPoint(Point.xyz(0.0, -2.0, 2.0))
        pointAssertThat(c0.topRight).isEqualToPoint(Point.xyz(0.0, 2.0, 2.0))
        pointAssertThat(c0.bottomLeft).isEqualToPoint(Point.xyz(0.0, -2.0, 0.0))
        pointAssertThat(c0.bottomRight).isEqualToPoint(Point.xyz(0.0, 2.0, 0.0))
        pointAssertThat(c0.top).isEqualToPoint(Point.xyz(0.0, 0.0, 1+r2))
        pointAssertThat(c0.bottom).isEqualToPoint(Point.xyz(0.0, 0.0, 1-r2))
        pointAssertThat(c0.left).isEqualToPoint(Point.xyz(0.0, -2*r2, 1.0))
        pointAssertThat(c0.right).isEqualToPoint(Point.xyz(0.0, 2*r2, 1.0))
        pointAssertThat(c0.center).isEqualToPoint(Point.xyz(0.0, 0.0, 1.0))

        val c1 = ConjugateBox.ofConicSection(e.complement())
        pointAssertThat(c1.topLeft).isEqualToPoint(Point.xyz(0.0, -2.0, 2.0))
        pointAssertThat(c1.topRight).isEqualToPoint(Point.xyz(0.0, 2.0, 2.0))
        pointAssertThat(c1.bottomLeft).isEqualToPoint(Point.xyz(0.0, -2.0, 0.0))
        pointAssertThat(c1.bottomRight).isEqualToPoint(Point.xyz(0.0, 2.0, 0.0))
        pointAssertThat(c0.top).isEqualToPoint(Point.xyz(0.0, 0.0, 1+r2))
        pointAssertThat(c0.bottom).isEqualToPoint(Point.xyz(0.0, 0.0, 1-r2))
        pointAssertThat(c0.left).isEqualToPoint(Point.xyz(0.0, -2*r2, 1.0))
        pointAssertThat(c0.right).isEqualToPoint(Point.xyz(0.0, 2*r2, 1.0))
        pointAssertThat(c0.center).isEqualToPoint(Point.xyz(0.0, 0.0, 1.0))

        val c2 = ConjugateBox.ofConicSection(l)
        pointAssertThat(c2.topLeft).isEqualToPoint(Point.xyz(0.0, 1.0, 1.0))
        pointAssertThat(c2.topRight).isEqualToPoint(Point.xyz(0.0, -1.0, -1.0))
        pointAssertThat(c2.bottomLeft).isEqualToPoint(Point.xyz(0.0, 1.0, 1.0))
        pointAssertThat(c2.bottomRight).isEqualToPoint(Point.xyz(0.0, -1.0, -1.0))
        pointAssertThat(c2.top).isEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
        pointAssertThat(c2.bottom).isEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
        pointAssertThat(c2.left).isEqualToPoint(Point.xyz(0.0, r2, r2))
        pointAssertThat(c2.right).isEqualToPoint(Point.xyz(0.0, -r2, -r2))
        pointAssertThat(c2.center).isEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
    }
}