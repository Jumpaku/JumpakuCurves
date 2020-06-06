package jumpaku.curves.fsc.test.generate.fit

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.generate.fit.WeightedParamPointJson
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class WeightedParamPointJsonTest {

    val wpp = WeightedParamPoint(
            point = Point.xyz(1.0, 2.0, 3.0),
            param = 3.0,
            weight = 2.0)

    @Test
    fun testWeightedParamPointJson() {
        println("WeightedParamPointJson")
        val a = WeightedParamPointJson.toJsonStr(wpp).parseJson().let { WeightedParamPointJson.fromJson(it) }
        assertThat(a, `is`(closeTo(wpp)))
    }
}