package jumpaku.fsc.test.snap.point

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.snap.point.PointSnapper
import org.junit.Test

class PointSnapResultTest {

    val baseGrid = Grid(
            spacing = 1.0,
            magnification = 4,
            origin = Point.xyz(0.0, 0.0, 0.0),
            axis = Vector.K,
            radian = 0.0,
            fuzziness = 0.25,
            resolution = 0)

    val snapper = PointSnapper(baseGrid, -1, 1)

    @Test
    fun testToString() {
        println("ToString")
        val r = 1/4.0
        val e = snapper.snap(Point.xr( 7/32.0, r))
        e.toString().parseJson().flatMap { PointSnapResult.fromJson(it) }.get().shouldEqualToPointSnapResult(e)
    }

}