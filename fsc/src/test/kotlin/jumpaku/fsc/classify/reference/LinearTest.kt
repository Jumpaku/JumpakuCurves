package jumpaku.fsc.classify.reference

import jumpaku.core.affine.Point
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.Interval
import jumpaku.core.curve.intervalAssertThat
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.curve.rationalbezier.conicSectionAssertThat
import org.junit.Test


class LinearTest {

    @Test
    fun testProperties() {
        println("Properties")
        val cs = ConicSection(Point.xyr(2.0, 1.0, 2.0), Point.xyr(2.5, 1.5, 1.5), Point.xyr(3.0, 2.0, 1.0), 1.0)
        val l = Linear(cs, Interval(-1.0, 3.0))
        conicSectionAssertThat(l.conicSection).isEqualConicSection(cs)
        intervalAssertThat(l.domain).isEqualToInterval(Interval(-1.0, 3.0))
    }

    @Test
    fun testFuzzyCurve() {
        println("FuzzyCurve")
        val cs = ConicSection(Point.xyr(2.0, 1.0, 2.0), Point.xyr(2.5, 1.5, 1.5), Point.xyr(3.0, 2.0, 1.0), 1.0)
        val l = Linear(cs, Interval(-1.0, 3.0))
        pointAssertThat(l.reference(-1.0)).isEqualToPoint(Point.xyr(1.0, 0.0, 5.0))
        pointAssertThat(l.reference(0.0)).isEqualToPoint(Point.xyr(2.0, 1.0, 2.0))
        pointAssertThat(l.reference(1.0)).isEqualToPoint(Point.xyr(3.0, 2.0, 1.0))
        pointAssertThat(l.reference(2.0)).isEqualToPoint(Point.xyr(4.0, 3.0, 4.0))
        pointAssertThat(l.reference(3.0)).isEqualToPoint(Point.xyr(5.0, 4.0, 7.0))
    }
}