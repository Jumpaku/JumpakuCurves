package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.geom.Point
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test


class WeightedParamPointTest {

    val wpp = WeightedParamPoint(
            point = Point.xyz(1.0, 2.0, 3.0),
            param = 3.0,
            weight = 2.0)

    @Test
    fun testToString() {
        println("ToString")
        val a = wpp.toString().parseJson().tryMap { WeightedParamPoint.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(wpp)))
    }
}