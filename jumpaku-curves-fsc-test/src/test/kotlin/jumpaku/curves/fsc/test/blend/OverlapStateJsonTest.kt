package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.blend.*
import jumpaku.curves.fsc.generate.Fuzzifier
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class OverlapStateJsonTest {

    val detected: OverlapState = OverlapState.Detected(
        OverlapMatrix.create(
            listOf(Point.xr(0.0, 2.0), Point.xr(4.0, 2.0), Point.xr(8.0, 2.0)),
            listOf(Point.xyr(0.0, 1.0, 2.0), Point.xyr(4.0, 1.0, 2.0), Point.xyr(8.0, 1.0, 2.0))
        ),
        OverlapState.Path(Grade(0.2), listOf(OverlapMatrix.Key(0, 0))),
        OverlapState.Path(Grade(0.5), listOf(OverlapMatrix.Key(0, 1), OverlapMatrix.Key(1, 2))),
        OverlapState.Path(Grade(0.2), listOf(OverlapMatrix.Key(2, 2)))
    )

    val notDetected: OverlapState = OverlapState.Detected(
        OverlapMatrix.create(
            listOf(Point.xr(0.0, 2.0), Point.xr(4.0, 2.0), Point.xr(8.0, 2.0)),
            listOf(Point.xyr(0.0, 1.0, 2.0), Point.xyr(4.0, 1.0, 2.0), Point.xyr(8.0, 1.0, 2.0))
        ),
        OverlapState.Path(Grade(0.2), listOf(OverlapMatrix.Key(0, 0))),
        OverlapState.Path(Grade(0.5), listOf(OverlapMatrix.Key(0, 1), OverlapMatrix.Key(1, 2))),
        OverlapState.Path(Grade(0.2), listOf(OverlapMatrix.Key(2, 2)))
    )

    @Test
    fun testOverlapStateJson1() {
        println("OverlapStateJson1")
        val a = OverlapStateJson.fromJson(OverlapStateJson.toJsonStr(detected).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(detected)))
    }

    @Test
    fun testOverlapStateJson2() {
        println("OverlapStateJson2")
        val a = OverlapStateJson.fromJson(OverlapStateJson.toJsonStr(notDetected).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(notDetected)))
    }
}