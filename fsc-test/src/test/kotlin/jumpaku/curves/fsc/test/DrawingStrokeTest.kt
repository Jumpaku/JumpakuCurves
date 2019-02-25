package jumpaku.curves.fsc.test

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.curve.polyline.shouldEqualToPolyline
import jumpaku.curves.core.test.shouldBeCloseTo
import jumpaku.curves.fsc.DrawingStroke
import org.junit.Test

class DrawingStrokeTest {

    val s = DrawingStroke(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
            ParamPoint(Point.xy(3.0, -2.0), 1.0),
            ParamPoint(Point.xy(5.0, -8.0), 2.0)))
    @Test
    fun testProperties() {
        println("Properties")
        s.beginParam.shouldBeCloseTo(0.0)
        s.endParam.shouldBeCloseTo(2.0)
        s.paramSpan.shouldBeCloseTo(2.0)
        Polyline(s.paramPoints).shouldEqualToPolyline(Polyline(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
                ParamPoint(Point.xy(3.0, -2.0), 1.0),
                ParamPoint(Point.xy(5.0, -8.0), 2.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = s.toString().parseJson().tryMap { DrawingStroke.fromJson(it) }.orThrow()
        Polyline(t.paramPoints).shouldEqualToPolyline(Polyline(listOf(ParamPoint(Point.xy(1.0, -1.0), 0.0),
                ParamPoint(Point.xy(3.0, -2.0), 1.0),
                ParamPoint(Point.xy(5.0, -8.0), 2.0))))
    }

}