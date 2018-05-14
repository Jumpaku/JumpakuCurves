package jumpaku.core.test.fit

import jumpaku.core.affine.ParamPoint
import jumpaku.core.affine.Point
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.fit.BezierFitter
import jumpaku.core.test.curve.bezier.shouldEqualToBezier
import org.junit.Test

class BezierFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = BezierFitter(b.degree).fit(data)
        f.shouldEqualToBezier(b)
    }

}