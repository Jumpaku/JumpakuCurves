package jumpaku.core.test.curve

import jumpaku.core.curve.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.core.json.parseJson
import org.junit.Test

class ParamPointTest {
    @Test
    fun testDivide() {
        println("Divide")
        val f0 = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        val f2 = ParamPoint(Point.xr(2.0, 20.0), 2.0)

        f0.lerp(0.3, f2).shouldEqualToParamPoint(ParamPoint(Point.xr(1.3, 13.0), 1.3))
        f0.lerp(-1.0, f2).shouldEqualToParamPoint(ParamPoint(Point.xr(0.0, 40.0), 0.0))
        f0.lerp(2.0, f2).shouldEqualToParamPoint(ParamPoint(Point.xr(3.0, 50.0), 3.0))
        f0.lerp(0.0, f2).shouldEqualToParamPoint(ParamPoint(Point.xr(1.0, 10.0), 1.0))
        f0.lerp(1.0, f2).shouldEqualToParamPoint(ParamPoint(Point.xr(2.0, 20.0), 2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        t.toString().parseJson().tryMap { ParamPoint.fromJson(it) }.orThrow().shouldEqualToParamPoint(t)
    }
}