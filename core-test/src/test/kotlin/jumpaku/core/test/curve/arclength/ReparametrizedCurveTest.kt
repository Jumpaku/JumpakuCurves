package jumpaku.core.test.curve.arclength

import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.test.geom.shouldEqualToPoint
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class ReparametrizedCurveTest {

    val R2 = FastMath.sqrt(2.0)
    val PI = FastMath.PI

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2*50, -R2*50), Point.xy(100.0, 0.0), -R2 / 2)

    val r = Reparametrizer.of(cs, cs.domain.sample(50))

    val rcs = ReparametrizedCurve(cs, r)

    @Test
    fun testEvaluate() {
        println("Evaluate")
        rcs.evaluate((0.0).coerceIn(r.range)).shouldEqualToPoint(Point.xy(0.0, 100.0), 1.0)
        rcs.evaluate((75*PI).coerceIn(r.range)).shouldEqualToPoint(Point.xy(-R2*50, -R2*50), 1.0)
        rcs.evaluate((150*PI).coerceIn(r.range)).shouldEqualToPoint(Point.xy(100.0, 0.0), 1.0)
    }
}