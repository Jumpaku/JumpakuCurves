package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.test.affine.shouldBePoint
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
        c0.topLeft.shouldBePoint(Point.xyz(0.0, -2.0, 2.0))
        c0.topRight.shouldBePoint(Point.xyz(0.0, 2.0, 2.0))
        c0.bottomLeft.shouldBePoint(Point.xyz(0.0, -2.0, 0.0))
        c0.bottomRight.shouldBePoint(Point.xyz(0.0, 2.0, 0.0))
        c0.top.shouldBePoint(Point.xyz(0.0, 0.0, 1+r2))
        c0.bottom.shouldBePoint(Point.xyz(0.0, 0.0, 1-r2))
        c0.left.shouldBePoint(Point.xyz(0.0, -2*r2, 1.0))
        c0.right.shouldBePoint(Point.xyz(0.0, 2*r2, 1.0))
        c0.center.shouldBePoint(Point.xyz(0.0, 0.0, 1.0))

        val c1 = ConjugateBox.ofConicSection(e.complement())
        c1.topLeft.shouldBePoint(Point.xyz(0.0, -2.0, 2.0))
        c1.topRight.shouldBePoint(Point.xyz(0.0, 2.0, 2.0))
        c1.bottomLeft.shouldBePoint(Point.xyz(0.0, -2.0, 0.0))
        c1.bottomRight.shouldBePoint(Point.xyz(0.0, 2.0, 0.0))
        c0.top.shouldBePoint(Point.xyz(0.0, 0.0, 1+r2))
        c0.bottom.shouldBePoint(Point.xyz(0.0, 0.0, 1-r2))
        c0.left.shouldBePoint(Point.xyz(0.0, -2*r2, 1.0))
        c0.right.shouldBePoint(Point.xyz(0.0, 2*r2, 1.0))
        c0.center.shouldBePoint(Point.xyz(0.0, 0.0, 1.0))

        val c2 = ConjugateBox.ofConicSection(l)
        c2.topLeft.shouldBePoint(Point.xyz(0.0, 1.0, 1.0))
        c2.topRight.shouldBePoint(Point.xyz(0.0, -1.0, -1.0))
        c2.bottomLeft.shouldBePoint(Point.xyz(0.0, 1.0, 1.0))
        c2.bottomRight.shouldBePoint(Point.xyz(0.0, -1.0, -1.0))
        c2.top.shouldBePoint(Point.xyz(0.0, 0.0, 0.0))
        c2.bottom.shouldBePoint(Point.xyz(0.0, 0.0, 0.0))
        c2.left.shouldBePoint(Point.xyz(0.0, r2, r2))
        c2.right.shouldBePoint(Point.xyz(0.0, -r2, -r2))
        c2.center.shouldBePoint(Point.xyz(0.0, 0.0, 0.0))
    }
}