package jumpaku.curves.core.test.curve.arclength

import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ReparametrizedCurveTest {

    val R2 = FastMath.sqrt(2.0)
    val PI = FastMath.PI

    val cs = ConicSection(Point.xy(0.0, 100.0), Point.xy(-R2 * 50, -R2 * 50), Point.xy(100.0, 0.0), -R2 / 2)

    val rcs = ReparametrizedCurve.of(cs, cs.domain.sample(15))

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(rcs.evaluate(0.0), `is`(closeTo(Point.xy(0.0, 100.0), 1.0)))
        assertThat(rcs.evaluate(0.5), `is`(closeTo(Point.xy(-R2 * 50, -R2 * 50), 1.0)))
        assertThat(rcs.evaluate(1.0), `is`(closeTo(Point.xy(100.0, 0.0), 1.0)))
    }

    @Test
    fun testRestrict() {
        println("Restrict")
        val r = rcs.restrict(1 / 3.0, 2 / 3.0)
        assertThat(r(0.0), `is`(closeTo(Point.xy(-100.0, 0.0), 1.0)))
        assertThat(r(0.5), `is`(closeTo(Point.xy(-R2 * 50, -R2 * 50), 1.0)))
        assertThat(r(1.0), `is`(closeTo(Point.xy(0.0, -100.0), 1.0)))
    }
}