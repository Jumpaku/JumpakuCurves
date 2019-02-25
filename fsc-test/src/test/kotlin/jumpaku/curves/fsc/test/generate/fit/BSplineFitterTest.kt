package jumpaku.curves.fsc.test.generate.fit

import io.vavr.collection.Array
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.test.curve.bspline.shouldEqualToBSpline
import org.junit.Test


class BSplineFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(1.0, 1.7), 3, 9))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = jumpaku.curves.fsc.generate.fit.BSplineFitter(b.degree, b.knotVector).fit(data)
        f.shouldEqualToBSpline(b)
    }

}