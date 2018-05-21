package jumpaku.core.test.affine

import jumpaku.core.geom.Point
import jumpaku.core.geom.WeightedParamPoint
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
        wpp.toString().parseJson().flatMap { WeightedParamPoint.fromJson(it) }.get().shouldEqualToWeightedParamPoint(wpp)
    }
}