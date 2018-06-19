package jumpaku.core.test.curve.arclength

import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.test.curve.shouldEqualToInterval
import jumpaku.core.test.shouldBeCloseTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test


class LinearTest {

    val l = Reparametrizer.MonotonicLinear(2.0, 7.0, Interval.ZERO_ONE)

    @Test
    fun testInvoke() {
        println("Invoke")
        l(0.0).shouldBeCloseTo(7.0)
        l(0.5).shouldBeCloseTo(8.0)
        l(1.0).shouldBeCloseTo(9.0)
    }

    @Test
    fun testInvert() {
        println("Invert")
        l.invert(7.0).shouldBeCloseTo(0.0)
        l.invert(8.0).shouldBeCloseTo(0.5)
        l.invert(9.0).shouldBeCloseTo(1.0)
    }
}

class ReparametrizerTest {

    val R2 = FastMath.sqrt(2.0)

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2*50, -R2*50), Point.xy(100.0, 0.0), -R2 / 2)

    val r = Reparametrizer.of(cs, cs.domain.sample(50))

    val PI = FastMath.PI

    @Test
    fun testRange() {
        println("Range")
        r.range.shouldEqualToInterval(Interval(0.0, 150*PI), 1.0)
    }

    @Test
    fun testToArcLength() {
        println("ToArcLength")
        r.toArcLength(0.0).shouldBeCloseTo(0.0, 1.0)
        r.toArcLength(0.5).shouldBeCloseTo(75*PI, 1.0)
        r.toArcLength(1.0).shouldBeCloseTo(150*PI, 1.0)
    }

    @Test
    fun testToOriginal() {
        println("ToOriginal")
        r.toOriginal((0.0).coerceIn(r.range)).shouldBeCloseTo(0.0, 1.0e-3)
        r.toOriginal((75*PI).coerceIn(r.range)).shouldBeCloseTo(0.5, 1.0e-3)
        r.toOriginal((150*PI).coerceIn(r.range)).shouldBeCloseTo(1.0, 1.0e-3)
    }
}