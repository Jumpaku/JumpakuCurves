package jumpaku.curves.core.test.curve.polyline

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.curve.polyline.LineSegmentJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class LineSegmentJsonTest {

    val l = LineSegment(
            ParamPoint(Point.xyr(0.0, 1.0, 1.0), -1.0),
            ParamPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

    @Test
    fun testLineSegmentJson() {
        println("LineSegmentJson")
        val a = LineSegmentJson.toJsonStr(l).parseJson().let { LineSegmentJson.fromJson(it) }
        Assert.assertThat(a.begin, Matchers.`is`(jumpaku.curves.core.test.geom.closeTo(l.begin)))
        Assert.assertThat(a.end, Matchers.`is`(jumpaku.curves.core.test.geom.closeTo(l.end)))
    }

}