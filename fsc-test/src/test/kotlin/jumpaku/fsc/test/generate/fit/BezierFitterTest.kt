package jumpaku.fsc.test.generate.fit

import jumpaku.core.curve.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.test.curve.bezier.shouldEqualToBezier
import org.junit.Test

class BezierFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = jumpaku.fsc.generate.fit.BezierFitter(b.degree).fit(data)
        f.shouldEqualToBezier(b)
    }

}