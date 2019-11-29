package jumpaku.curves.core.test.curve.arclength

import jumpaku.commons.test.math.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.MonotonicQuadratic
import jumpaku.curves.core.curve.arclength.Reparametrizer
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.closeTo
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class ReparametrizerTest {

    val R2 = FastMath.sqrt(2.0)

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2 * 50, -R2 * 50), Point.xy(100.0, 0.0), -R2 / 2)

    val r = Reparametrizer.of(cs, cs.domain.sample(100))

    val PI = FastMath.PI

    @Test
    fun testRange() {
        println("Range")
        assertThat(r.range, `is`(closeTo(Interval.ZERO_ONE)))
    }

    @Test
    fun testToArcLength() {
        println("ToArcLength")
        assertThat(r.toArcLengthRatio(0.0), `is`(closeTo(0.0, 1.0)))
        assertThat(r.toArcLengthRatio(0.5), `is`(closeTo(0.5, 1.0)))
        assertThat(r.toArcLengthRatio(1.0), `is`(closeTo(1.0, 1.0)))
    }

    @Test
    fun testToOriginal() {
        println("ToOriginal")
        assertThat(r.toOriginal((0.0).coerceIn(r.range)), `is`(closeTo(0.0, 1.0e-3)))
        assertThat(r.toOriginal((0.5).coerceIn(r.range)), `is`(closeTo(0.5, 1.0e-3)))
        assertThat(r.toOriginal((1.0).coerceIn(r.range)), `is`(closeTo(1.0, 1.0e-3)))
    }
}


class MonotonicQuadraticTest {

    val q0 = MonotonicQuadratic(2.0, 10.0, 14.0, Interval(2.0, 4.0))

    val q1 = MonotonicQuadratic(2.0, 6.0, 14.0, Interval(2.0, 4.0))

    val q2 = MonotonicQuadratic(2.0, 8.0, 14.0, Interval(2.0, 4.0))

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(q0(2.0), `is`(closeTo(2.0)))
        assertThat(q0(3.0), `is`(closeTo(9.0)))
        assertThat(q0(4.0), `is`(closeTo(14.0)))

        assertThat(q1(2.0), `is`(closeTo(2.0)))
        assertThat(q1(3.0), `is`(closeTo(7.0)))
        assertThat(q1(4.0), `is`(closeTo(14.0)))

        assertThat(q2(2.0), `is`(closeTo(2.0)))
        assertThat(q2(3.0), `is`(closeTo(8.0)))
        assertThat(q2(4.0), `is`(closeTo(14.0)))
    }

    @Test
    fun testInvert() {
        println("Invert")
        assertThat(q0.invert(2.0), `is`(closeTo(2.0)))
        assertThat(q0.invert(9.0), `is`(closeTo(3.0)))
        assertThat(q0.invert(14.0), `is`(closeTo(4.0)))

        assertThat(q1.invert(2.0), `is`(closeTo(2.0)))
        assertThat(q1.invert(7.0), `is`(closeTo(3.0)))
        assertThat(q1.invert(14.0), `is`(closeTo(4.0)))

        assertThat(q2.invert(2.0), `is`(closeTo(2.0)))

        assertThat(q2.invert(5.0), `is`(closeTo(2.5)))
        assertThat(q2.invert(8.0), `is`(closeTo(3.0)))
        assertThat(q2.invert(11.0), `is`(closeTo(3.5)))
        assertThat(q2.invert(14.0), `is`(closeTo(4.0)))
    }
}