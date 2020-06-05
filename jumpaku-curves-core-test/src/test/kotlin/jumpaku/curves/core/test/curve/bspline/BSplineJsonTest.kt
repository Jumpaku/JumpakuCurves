package jumpaku.curves.core.test.curve.bspline

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class BSplineJsonTest {

    val clamped = BSpline(
            listOf(
                    Point.xyr(-1.0, 0.0, 0.0),
                    Point.xyr(-1.0, 1.0, 1.0),
                    Point.xyr(0.0, 1.0, 2.0),
                    Point.xyr(0.0, 0.0, 1.0),
                    Point.xyr(1.0, 0.0, 0.0)),
            KnotVector.clamped(Interval(3.0, 4.0), 3, 9))

    @Test
    fun testBSplineJson() {
        println("BSplineJson")
        val a = BSplineJson.toJsonStr(clamped).parseJson().let { BSplineJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(clamped)))
    }

}