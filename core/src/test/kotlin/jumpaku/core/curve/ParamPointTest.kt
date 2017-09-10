package jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.affine.Point
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.junit.Test

class ParamPointTest {
    @Test
    fun testDivide() {
        println("Divide")
        val f0 = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        val f2 = ParamPoint(Point.xr(2.0, 20.0), 2.0)

        paramPointAssertThat(f0.divide(0.3, f2)).isEqualToParamPoint(ParamPoint(Point.xr(1.3, 13.0), 1.3))
        paramPointAssertThat(f0.divide(-1.0, f2)).isEqualToParamPoint(ParamPoint(Point.xr(0.0, 40.0), 0.0))
        paramPointAssertThat(f0.divide(2.0, f2)).isEqualToParamPoint(ParamPoint(Point.xr(3.0, 50.0), 3.0))
        paramPointAssertThat(f0.divide(0.0, f2)).isEqualToParamPoint(ParamPoint(Point.xr(1.0, 10.0), 1.0))
        paramPointAssertThat(f0.divide(1.0, f2)).isEqualToParamPoint(ParamPoint(Point.xr(2.0, 20.0), 2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        paramPointAssertThat(t.toString().parseToJson().get().paramPoint).isEqualToParamPoint(t)
    }

}