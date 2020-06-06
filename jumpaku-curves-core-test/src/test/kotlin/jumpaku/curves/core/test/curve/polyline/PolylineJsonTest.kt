package jumpaku.curves.core.test.curve.polyline

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.polyline.PolylineJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class PolylineJsonTest {

    val pl = Polyline.byArcLength(Point.xyr(-1.0, 1.0, 2.0), Point.xyr(1.0, 1.0, 1.0), Point.xyr(1.0, -3.0, 3.0), Point.xyzr(1.0, -3.0, 1.5, 2.0))

    @Test
    fun testPolylineJson() {
        println("PolylineJson")
        val a = PolylineJson.toJsonStr(pl).parseJson().let { PolylineJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(pl)))
    }

}