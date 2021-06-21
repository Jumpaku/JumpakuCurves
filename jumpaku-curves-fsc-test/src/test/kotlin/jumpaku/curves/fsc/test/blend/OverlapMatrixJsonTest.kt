package jumpaku.curves.fsc.test.blend

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.blend.BlenderJson
import jumpaku.curves.fsc.blend.OverlapMatrix
import jumpaku.curves.fsc.blend.OverlapMatrixJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class OverlapMatrixJsonTest {

    val osm: OverlapMatrix = OverlapMatrix.create(
        listOf(
            Point.xr(0.0, 2.0),
            Point.xr(4.0, 2.0),
            Point.xr(8.0, 2.0),
        ), listOf(
            Point.xyr(0.0, 1.0, 2.0),
            Point.xyr(4.0, 1.0, 2.0),
            Point.xyr(8.0, 1.0, 2.0),
        )
    )

    @Test
    fun testOverlapMatrixJson() {
        println("OverlapMatrixJson")
        val a = OverlapMatrixJson.fromJson(OverlapMatrixJson.toJsonStr(osm).parseJson())
        Assert.assertThat(a, Matchers.`is`(closeTo(osm)))
    }
}