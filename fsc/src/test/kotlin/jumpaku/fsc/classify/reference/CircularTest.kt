package jumpaku.fsc.classify.reference

import org.apache.commons.math3.util.FastMath
import jumpaku.core.affine.Point
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.Interval
import jumpaku.core.curve.intervalAssertThat
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.conicSectionAssertThat
import org.junit.Test


class CircularTest {

    val R2 = FastMath.sqrt(2.0)

    @Test
    fun testProperties() {
        println("Properties")
        val cs = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        val c = Circular(cs, Interval(-0.5, 1.5))
        conicSectionAssertThat(c.conicSection).isEqualConicSection(cs)
        intervalAssertThat(c.domain).isEqualToInterval(Interval(-0.5, 1.5))
    }

    @Test
    fun testFuzzyCurve() {
        println("FuzzyCurve")
        val cs = ConicSection(Point.xyr(0.0, 1.0, 1.0), Point.xyr(R2 / 2, R2 / 2, 2.0), Point.xyr(1.0, 0.0, 3.0), R2 / 2)
        val c = Circular(cs, Interval(-0.5, 1.5))
        pointAssertThat(c.reference(-0.5)).isEqualToPoint(Point.xyr(-1/R2, -1/R2, 14+8*R2))
        pointAssertThat(c.reference(-0.25)).isEqualToPoint(Point.xyr((1-3*R2)/(10-3*R2), (9-3*R2)/(10-3*R2), (48+6*R2)/(10-3*R2)))
        pointAssertThat(c.reference(0.0)).isEqualToPoint(Point.xyr(0.0, 1.0, 1.0))
        pointAssertThat(c.reference(0.25)).isEqualToPoint(Point.xyr((3*R2+1)/(3*R2+10), (3*R2+9)/(3*R2+10), (24+6*R2)/(10+3*R2)))
        pointAssertThat(c.reference(0.5)).isEqualToPoint(Point.xyr(1/R2, 1/R2, 2.0))
        pointAssertThat(c.reference(0.75)).isEqualToPoint(Point.xyr((3*R2+9)/(3*R2+10), (3*R2+1)/(3*R2+10), (32+6*R2)/(10+3*R2)))
        pointAssertThat(c.reference(1.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 3.0))
        pointAssertThat(c.reference(1.25)).isEqualToPoint(Point.xyr((9-3*R2)/(10-3*R2), (1-3*R2)/(10-3*R2), (56+6*R2)/(10-3*R2)))
        pointAssertThat(c.reference(1.5)).isEqualToPoint(Point.xyr(-1/R2, -1/R2, 14+8*R2))
    }
}