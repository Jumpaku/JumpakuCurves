package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapResult
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapResultTest {

    val baseGrid = Grid(
            baseSpacing = 1.0,
            magnification = 4,
            origin = Point.xyz(0.0, 0.0, 0.0),
            rotation = Rotate(Vector.K, 0.0),
            baseFuzziness = 0.25)

    val snapper = MFGS(-1, 1)

    @Test
    fun testToString() {
        println("ToString")
        val r = 1 / 4.0
        val e = snapper.snap(baseGrid, Point.xr(7 / 32.0, r))
        assertThat(e.isDefined, `is`(true))
        assertThat(e.orThrow().toString().parseJson().tryMap { PointSnapResult.fromJson(it) }.orThrow(), `is`(closeTo(e.orThrow())))
    }

}