package jumpaku.fsc.test.snap.point

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.snap.point.MFGS
import org.amshove.kluent.shouldBeTrue
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
        val r = 1/4.0
        val e = snapper.snap(baseGrid, Point.xr( 7/32.0, r))
        e.isDefined.shouldBeTrue()
        e.orThrow().toString().parseJson().tryFlatMap { PointSnapResult.fromJson(it) }.orThrow().shouldEqualToPointSnapResult(e.orThrow())
    }

}