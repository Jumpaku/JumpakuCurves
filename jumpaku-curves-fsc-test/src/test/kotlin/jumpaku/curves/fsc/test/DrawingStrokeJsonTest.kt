package jumpaku.curves.fsc.test

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.polyline.closeTo
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.DrawingStrokeJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class DrawingStrokeJsonTest {

    val s = DrawingStroke(listOf(
            ParamPoint(Point.xy(1.0, -1.0), 0.0),
            ParamPoint(Point.xy(3.0, -2.0), 1.0),
            ParamPoint(Point.xy(5.0, -8.0), 2.0)))

    @Test
    fun testDrawingStrokeJson() {
        println("DrawingStrokeJson")
        val a = DrawingStrokeJson.toJsonStr(s).parseJson().let { DrawingStrokeJson.fromJson(it) }
        Assert.assertThat(Polyline(a.inputData), Matchers.`is`(closeTo(Polyline(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
                ParamPoint(Point.xy(3.0, -2.0), 1.0),
                ParamPoint(Point.xy(5.0, -8.0), 2.0))))))
    }

}