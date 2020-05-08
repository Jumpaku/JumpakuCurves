package jumpaku.curves.fsc.test.merge

import jumpaku.commons.json.parseJson
import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.merge.MergeData
import jumpaku.curves.fsc.test.generate.fit.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class MergeDataTest {

    val g = Grade(0.75)
    val f = listOf(ParamPoint(Point.xy(1.0, 2.0), 3.0), ParamPoint(Point.xy(4.0, 5.0), 6.0))
    val m = listOf(WeightedParamPoint(Point.xy(7.0, 8.0), 9.0, 10.0), WeightedParamPoint(Point.xy(11.0, 12.0), 13.0, 14.0))
    val b = listOf(ParamPoint(Point.xy(15.0, 16.0), 17.0), ParamPoint(Point.xy(18.0, 19.0), 20.0))
    val bd = MergeData(g, f, b, m)

    @Test
    fun testGrade() {
        println("Grade")
        assertThat(bd.grade.value, `is`(closeTo(g.value)))
    }

    @Test
    fun testIntervals() {
        println("Intervals")
        assertThat(bd.domain, `is`(closeTo(Interval(3.0, 20.0))))
        assertThat(bd.frontInterval.isDefined, `is`(true))
        assertThat(bd.frontInterval.orThrow(), `is`(closeTo(Interval(3.0, 6.0))))
        assertThat(bd.backInterval.isDefined, `is`(true))
        assertThat(bd.backInterval.orThrow(), `is`(closeTo(Interval(17.0, 20.0))))
        assertThat(bd.mergeInterval, `is`(closeTo(Interval(9.0, 13.0))))
    }

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
        val a = MergeData.fromJson(bd.toJsonString().parseJson().orThrow())
        assertThat(a, `is`(closeTo(bd)))
    }
}