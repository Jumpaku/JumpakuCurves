package jumpaku.curves.fsc.test.snap

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridJson
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class GridJsonTest {

    val p2 = FastMath.PI / 2

    val baseGrid = Grid(
            baseSpacing = 4.0,
            magnification = 2,
            origin = Point.xyz(4.0, 4.0, 0.0),
            rotation = Rotate(Vector.K, p2),
            baseFuzziness = 2.0)

    @Test
    fun testGridJson() {
        println("GridJson")
        Assert.assertThat(GridJson.toJsonStr(baseGrid).parseJson().let { GridJson.fromJson(it) }, Matchers.`is`(closeTo(baseGrid)))
    }
}