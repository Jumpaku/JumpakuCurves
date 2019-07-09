package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.weighted
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.fsc.blend.BlendData
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class BlendDataTest {

    val f = listOf(ParamPoint(Point.xy(1.0, 2.0), 3.0), ParamPoint(Point.xy(4.0, 5.0), 6.0))
    val m = listOf(WeightedParamPoint(Point.xy(7.0, 8.0), 9.0, 10.0), WeightedParamPoint(Point.xy(11.0, 12.0), 13.0, 14.0))
    val b = listOf(ParamPoint(Point.xy(15.0, 16.0), 17.0), ParamPoint(Point.xy(18.0, 19.0), 20.0))

    val bd = BlendData(f, b, m)

    @Test
    fun testAggregated() {
        println("Aggregated")
        val a = bd.aggregated
        val e = listOf(
                ParamPoint(Point.xy(1.0, 2.0), 3.0).weighted(1.0),
                ParamPoint(Point.xy(4.0, 5.0), 6.0).weighted(1.0),
                WeightedParamPoint(Point.xy(7.0, 8.0), 9.0, 10.0),
                WeightedParamPoint(Point.xy(11.0, 12.0), 13.0, 14.0),
                ParamPoint(Point.xy(15.0, 16.0), 17.0).weighted(1.0),
                ParamPoint(Point.xy(18.0, 19.0), 20.0).weighted(1.0))
        assertThat(a.size, `is`(6))
        for (i in 0..5) {
            assertThat(a[i], `is`(closeTo(e[i])))
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = BlendData.fromJson(bd.toJsonString().parseJson().orThrow())
        assertThat(a, `is`(closeTo(bd)))
    }
}