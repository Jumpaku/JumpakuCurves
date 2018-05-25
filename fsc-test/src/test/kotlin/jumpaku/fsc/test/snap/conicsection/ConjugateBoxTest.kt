package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.geom.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.test.geom.shouldEqualToPoint
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
        c0.topLeft.shouldEqualToPoint(Point.xyz(0.0, -2.0, 2.0))
        c0.topRight.shouldEqualToPoint(Point.xyz(0.0, 2.0, 2.0))
        c0.bottomLeft.shouldEqualToPoint(Point.xyz(0.0, -2.0, 0.0))
        c0.bottomRight.shouldEqualToPoint(Point.xyz(0.0, 2.0, 0.0))
        c0.top.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1+r2))
        c0.bottom.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1-r2))
        c0.left.shouldEqualToPoint(Point.xyz(0.0, -2*r2, 1.0))
        c0.right.shouldEqualToPoint(Point.xyz(0.0, 2*r2, 1.0))
        c0.center.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1.0))

        val c1 = ConjugateBox.ofConicSection(e.complement())
        c1.topLeft.shouldEqualToPoint(Point.xyz(0.0, -2.0, 2.0))
        c1.topRight.shouldEqualToPoint(Point.xyz(0.0, 2.0, 2.0))
        c1.bottomLeft.shouldEqualToPoint(Point.xyz(0.0, -2.0, 0.0))
        c1.bottomRight.shouldEqualToPoint(Point.xyz(0.0, 2.0, 0.0))
        c0.top.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1+r2))
        c0.bottom.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1-r2))
        c0.left.shouldEqualToPoint(Point.xyz(0.0, -2*r2, 1.0))
        c0.right.shouldEqualToPoint(Point.xyz(0.0, 2*r2, 1.0))
        c0.center.shouldEqualToPoint(Point.xyz(0.0, 0.0, 1.0))

        val c2 = ConjugateBox.ofConicSection(l)
        c2.topLeft.shouldEqualToPoint(Point.xyz(0.0, 1.0, 1.0))
        c2.topRight.shouldEqualToPoint(Point.xyz(0.0, -1.0, -1.0))
        c2.bottomLeft.shouldEqualToPoint(Point.xyz(0.0, 1.0, 1.0))
        c2.bottomRight.shouldEqualToPoint(Point.xyz(0.0, -1.0, -1.0))
        c2.top.shouldEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
        c2.bottom.shouldEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
        c2.left.shouldEqualToPoint(Point.xyz(0.0, r2, r2))
        c2.right.shouldEqualToPoint(Point.xyz(0.0, -r2, -r2))
        c2.center.shouldEqualToPoint(Point.xyz(0.0, 0.0, 0.0))
    }
}