package org.jumpaku.core.fitting

import io.vavr.API
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.bspline.bSplineAssertThat
import org.junit.Test


class BSplineFittingTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = BSpline(
                API.Array(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                Knot.clampedUniformKnots(Interval(1.0, 1.7), 3, 9))
        val data = b.domain.sample(10).map { TimeSeriesPoint(b(it), it) }
        val f = BSplineFitting(b.degree, b.knots).fit(data)
        bSplineAssertThat(f).isEqualToBSpline(b)
    }

}