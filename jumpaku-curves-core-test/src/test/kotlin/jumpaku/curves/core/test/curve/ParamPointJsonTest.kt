package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.ParamPointJson
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class ParamPointJsonTest {

    @Test
    fun testToString() {
        println("ToString")
        val t = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        val a = ParamPointJson.toJsonStr(t).parseJson().let { ParamPointJson.fromJson(it) }
        Assert.assertThat(a, CoreMatchers.`is`(closeTo(t)))
    }
}