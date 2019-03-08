package jumpaku.curves.core.test.curve.polyline

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.geom.Point
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.test.geom.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LineSegmentTest {

    val l = LineSegment(
            ParamPoint(Point.xyr(0.0, 1.0, 1.0), -1.0),
            ParamPoint(Point.xyr(1.0, 0.0, 3.0), 1.0))

    @Test
    fun testToString() {
        println("ToString")
        val a = l.toString().parseJson().tryMap { LineSegment.fromJson(it) }.orThrow()
        assertThat(a.begin, `is`(closeTo(l.begin)))
        assertThat(a.end, `is`(closeTo(l.end)))
    }

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(l(-1.0), `is`(closeTo(Point.xyr(0.0, 1.0, 1.0))))
        assertThat(l(0.0), `is`(closeTo(Point.xyr(0.5, 0.5, 2.0))))
        assertThat(l(1.0), `is`(closeTo(Point.xyr(1.0, 0.0, 3.0))))
    }
}