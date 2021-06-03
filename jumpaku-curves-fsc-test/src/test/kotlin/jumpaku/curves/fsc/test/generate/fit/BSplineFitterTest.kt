package jumpaku.curves.fsc.test.generate.fit

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.bspline.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class BSplineFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = BSpline(
                listOf(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(1.0, 1.7), 3, 9))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = jumpaku.curves.fsc.generate.fit.BSplineFitter(b.degree, b.knotVector).fit(data)
        assertThat(f, `is`(closeTo(b)))
    }

}