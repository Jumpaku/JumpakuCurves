package jumpaku.curves.core.test.curve.bezier

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bezier.BezierJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class BezierJsonTest {

    private val bc = Bezier(Point.xyr(-2.0, 0.0, 1.0), Point.xyr(-1.0, 0.0, 2.0), Point.xy(0.0, 2.0), Point.xyr(1.0, 0.0, 2.0), Point.xyr(2.0, 0.0, 1.0))

    @Test
    fun testBezierJson() {
        println("BezierJson")
        val a = BezierJson.toJsonStr(bc).parseJson().let { BezierJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(bc)))
    }

}