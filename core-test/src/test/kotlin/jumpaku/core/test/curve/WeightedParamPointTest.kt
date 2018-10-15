package jumpaku.core.test.curve

import jumpaku.core.geom.Point
import jumpaku.core.curve.WeightedParamPoint
import jumpaku.core.json.parseJson
import org.junit.jupiter.api.Test


class WeightedParamPointTest {

    val wpp = WeightedParamPoint(
            point = Point.xyz(1.0, 2.0, 3.0),
            param = 3.0,
            weight = 2.0)

    @Test
    fun testToString() {
        println("ToString")
        wpp.toString().parseJson().tryFlatMap { WeightedParamPoint.fromJson(it) }.orThrow().shouldEqualToWeightedParamPoint(wpp)
    }
}