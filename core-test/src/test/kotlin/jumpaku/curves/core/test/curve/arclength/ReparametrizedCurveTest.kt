package jumpaku.curves.core.test.curve.arclength

import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class ReparametrizedCurveTest {

    val R2 = FastMath.sqrt(2.0)
    val PI = FastMath.PI

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2*50, -R2*50), Point.xy(100.0, 0.0), -R2 / 2)

    val rcs = ReparametrizedCurve.of(cs, cs.domain.sample(15))

    @Test
    fun testEvaluate() {
        println("Evaluate")
        rcs.evaluate(0.0).shouldEqualToPoint(Point.xy(0.0, 100.0), 1.0)
        rcs.evaluate(0.5).shouldEqualToPoint(Point.xy(-R2*50, -R2*50), 1.0)
        rcs.evaluate(1.0).shouldEqualToPoint(Point.xy(100.0, 0.0), 1.0)
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val r = rcs.restrict(1/3.0, 2/3.0)
        r(0.0).shouldEqualToPoint(Point.xy(-100.0, 0.0), 1.0)
        r(0.5).shouldEqualToPoint(Point.xy(-R2*50, -R2*50), 1.0)
        r(1.0).shouldEqualToPoint(Point.xy(0.0, -100.0), 1.0)
    }
}