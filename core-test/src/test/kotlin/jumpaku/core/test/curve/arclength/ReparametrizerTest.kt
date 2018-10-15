package jumpaku.core.test.curve.arclength

import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.MonotonicQuadratic
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.test.curve.shouldEqualToInterval
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.apache.commons.math3.util.FastMath
import org.junit.Test


class ReparametrizerTest {

    val R2 = FastMath.sqrt(2.0)

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2*50, -R2*50), Point.xy(100.0, 0.0), -R2 / 2)

    val r = Reparametrizer.of(cs, cs.domain.sample(100))

    val PI = FastMath.PI

    @Test
    fun testRange() {
        println("Range")
        r.range.shouldEqualToInterval(Interval.ZERO_ONE)
    }

    @Test
    fun testToArcLength() {
        println("ToArcLength")
        r.toArcLengthRatio(0.0).shouldBeCloseTo(0.0, 1.0)
        r.toArcLengthRatio(0.5).shouldBeCloseTo(0.5, 1.0)
        r.toArcLengthRatio(1.0).shouldBeCloseTo(1.0, 1.0)
    }

    @Test
    fun testToOriginal() {
        println("ToOriginal")
        r.toOriginal((0.0).coerceIn(r.range)).shouldBeCloseTo(0.0, 1.0e-3)
        r.toOriginal((0.5).coerceIn(r.range)).shouldBeCloseTo(0.5, 1.0e-3)
        r.toOriginal((1.0).coerceIn(r.range)).shouldBeCloseTo(1.0, 1.0e-3)
    }
}


class MonotonicQuadraticTest {

    val q0 = MonotonicQuadratic(2.0, 10.0, 14.0, Interval(2.0, 4.0))

    val q1 = MonotonicQuadratic(2.0, 6.0, 14.0, Interval(2.0, 4.0))

    val q2 = MonotonicQuadratic(2.0, 8.0, 14.0, Interval(2.0, 4.0))

    @Test
    fun testInvoke() {
        println("Invoke")
        q0(2.0).shouldBeCloseTo(2.0)
        q0(3.0).shouldBeCloseTo(9.0)
        q0(4.0).shouldBeCloseTo(14.0)

        q1(2.0).shouldBeCloseTo(2.0)
        q1(3.0).shouldBeCloseTo(7.0)
        q1(4.0).shouldBeCloseTo(14.0)

        q2(2.0).shouldBeCloseTo(2.0)
        q2(3.0).shouldBeCloseTo(8.0)
        q2(4.0).shouldBeCloseTo(14.0)
    }

    @Test
    fun testInvert() {
        println("Invert")
        q0.invert(2.0).shouldBeCloseTo(2.0)
        q0.invert(9.0).shouldBeCloseTo(3.0)
        q0.invert(14.0).shouldBeCloseTo(4.0)

        q1.invert(2.0).shouldBeCloseTo(2.0)
        q1.invert(7.0).shouldBeCloseTo(3.0)
        q1.invert(14.0).shouldBeCloseTo(4.0)

        q2.invert(2.0).shouldBeCloseTo(2.0)

        q2.invert(5.0).shouldBeCloseTo(2.5)
        q2.invert(8.0).shouldBeCloseTo(3.0)
        q2.invert(11.0).shouldBeCloseTo(3.5)
        q2.invert(14.0).shouldBeCloseTo(4.0)
    }
}