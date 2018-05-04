package jumpaku.core.testold.fit

import io.vavr.API
import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.BSplineFitter
import jumpaku.core.testold.curve.bspline.bSplineAssertThat
import org.junit.Test


class BSplineFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = BSpline(
                API.Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(1.0, 1.7), 3, 9))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = BSplineFitter(b.degree, b.knotVector)
                .fit(data)
        bSplineAssertThat(f).isEqualToBSpline(b)
    }

}