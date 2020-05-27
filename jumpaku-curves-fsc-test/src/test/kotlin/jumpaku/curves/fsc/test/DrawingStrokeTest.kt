package jumpaku.curves.fsc.test

import jumpaku.commons.json.parseJson
import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.polyline.closeTo
import jumpaku.curves.core.transform.RotateJson
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.DrawingStrokeJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class DrawingStrokeTest {

    val s = DrawingStroke(listOf(
            ParamPoint(Point.xy(1.0, -1.0), 0.0),
            ParamPoint(Point.xy(3.0, -2.0), 1.0),
            ParamPoint(Point.xy(5.0, -8.0), 2.0)))

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(s.beginParam, `is`(closeTo(0.0)))
        assertThat(s.endParam, `is`(closeTo(2.0)))
        assertThat(s.paramSpan, `is`(closeTo(2.0)))
        assertThat(Polyline(s.inputData), `is`(closeTo(Polyline(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
                ParamPoint(Point.xy(3.0, -2.0), 1.0),
                ParamPoint(Point.xy(5.0, -8.0), 2.0))))))
    }
}

class DrawingStrokeJsonTest {

    val s = DrawingStroke(listOf(
            ParamPoint(Point.xy(1.0, -1.0), 0.0),
            ParamPoint(Point.xy(3.0, -2.0), 1.0),
            ParamPoint(Point.xy(5.0, -8.0), 2.0)))

    @Test
    fun testDrawingStrokeJson() {
        println("DrawingStrokeJson")
        val a = DrawingStrokeJson.toJsonStr(s).parseJson().let { DrawingStrokeJson.fromJson(it) }
        assertThat(Polyline(a.inputData), `is`(closeTo(Polyline(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
                ParamPoint(Point.xy(3.0, -2.0), 1.0),
                ParamPoint(Point.xy(5.0, -8.0), 2.0))))))
    }

}