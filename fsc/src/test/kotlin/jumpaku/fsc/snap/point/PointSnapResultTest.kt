package jumpaku.fsc.snap.point

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.gridAssertThat
import jumpaku.fsc.snap.gridPointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

fun pointSnapResultAssertThat(actual: PointSnapResult): PointSnapResultAssert = PointSnapResultAssert(actual)

class PointSnapResultAssert(actual: PointSnapResult) : AbstractAssert<PointSnapResultAssert, PointSnapResult>(actual, PointSnapResultAssert::class.java) {

    fun isEqualToPointSnapResult(expected: PointSnapResult, eps: Double = 1.0e-10): PointSnapResultAssert {
        isNotNull

        Assertions.assertThat(actual.grade.value).isEqualTo(expected.grade.value, withPrecision(eps))
        pointAssertThat(actual.worldPoint).isEqualToPoint(expected.worldPoint)
        gridPointAssertThat(actual.gridPoint).isEqualToGridPoint(expected.gridPoint)
        gridAssertThat(actual.grid).isEqualToGrid(expected.grid)

        return this
    }
}

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
        pointSnapResultAssertThat(e.toString().parseJson().flatMap { PointSnapResult.fromJson(it) }.get())
                .isEqualToPointSnapResult(e)
    }

}