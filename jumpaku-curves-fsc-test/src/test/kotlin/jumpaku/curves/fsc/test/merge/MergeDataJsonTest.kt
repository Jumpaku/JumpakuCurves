package jumpaku.curves.fsc.test.merge

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.merge.MergeData
import jumpaku.curves.fsc.merge.MergeDataJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class MergeDataJsonTest {

    val g = Grade(0.75)
    val f = listOf(ParamPoint(Point.xy(1.0, 2.0), 3.0), ParamPoint(Point.xy(4.0, 5.0), 6.0))
    val m = listOf(WeightedParamPoint(Point.xy(7.0, 8.0), 9.0, 10.0), WeightedParamPoint(Point.xy(11.0, 12.0), 13.0, 14.0))
    val b = listOf(ParamPoint(Point.xy(15.0, 16.0), 17.0), ParamPoint(Point.xy(18.0, 19.0), 20.0))
    val bd = MergeData(g, f, b, m)

    @Test
    fun testMergeDataJson() {
        println("MergeDataJson")
        val a = MergeDataJson.fromJson(MergeDataJson.toJsonStr(bd).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(bd)))
    }
}