package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineDerivative
import jumpaku.curves.core.curve.bspline.BSplineDerivativeJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class BSplineDerivativeJsonTest{

    val b = BSplineDerivative(BSpline(
            listOf(Point.xyr(-1.0, 0.0, 0.0), Point.xyr(-1.0, 1.0, 1.0), Point.xyr(0.0, 1.0, 2.0), Point.xyr(0.0, 0.0, 1.0), Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9)))

    @Test
    fun testBSplineDerivativeJson() {
        println("BSplineDerivativeJson")
        val a = BSplineDerivativeJson.toJsonStr(b).parseJson().let { BSplineDerivativeJson.fromJson(it) }
        Assert.assertThat(a.toBSpline(), CoreMatchers.`is`(closeTo(b.toBSpline())))
    }

}