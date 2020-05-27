package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.PointSnapResult
import jumpaku.curves.fsc.snap.point.PointSnapResultJson
import jumpaku.curves.fsc.snap.point.transformToWorld
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapResultTest {

    val baseGrid = Grid(
            baseSpacing = 4.0,
            magnification = 2,
            origin = Point.xyz(4.0, 4.0, 0.0),
            rotation = Rotate(Vector.K, FastMath.PI / 2),
            baseFuzziness = 2.0)


    @Test
    fun testTransformToWorld() {
        println("LocalToWorld")
        fun assertPointSnapResult(gridPoint: GridPoint, resolution: Int, expected: Point) {
            val actual = baseGrid.transformToWorld(PointSnapResult(resolution, gridPoint, Grade.TRUE))
            assertThat(actual, `is`(closeTo(expected)))
        }
        assertPointSnapResult(GridPoint(0, 0, 0),  0, Point.xyr(4.0, 4.0, 2.0))
        assertPointSnapResult(GridPoint(1, 0, 0),  0, Point.xyr(4.0, 8.0, 2.0))
        assertPointSnapResult(GridPoint(0, 1, 0),  0, Point.xyr(0.0, 4.0, 2.0))

        assertPointSnapResult(GridPoint(0, 0, 0), -1, Point.xyr(4.0, 4.0, 4.0))
        assertPointSnapResult(GridPoint(1, 0, 0), -1, Point.xyr(4.0, 12.0, 4.0))
        assertPointSnapResult(GridPoint(0, 1, 0), -1, Point.xyr(-4.0, 4.0, 4.0))

        assertPointSnapResult(GridPoint(0, 0, 0),  1, Point.xyr(4.0, 4.0, 1.0))
        assertPointSnapResult(GridPoint(1, 0, 0),  1, Point.xyr(4.0, 6.0, 1.0))
        assertPointSnapResult(GridPoint(0, 1, 0),  1, Point.xyr(2.0, 4.0, 1.0))
    }

}

class PointSnapResultJsonTest{

    @Test
    fun testPointSnapResultJson() {
        println("PointSnapResultJson")
        val a = PointSnapResult(5, GridPoint(1,2,-8), Grade(0.6))
        val e = PointSnapResultJson.toJsonStr(a).parseJson().let { PointSnapResultJson.fromJson(it) }
        assertThat(a, `is`(closeTo(e)))
    }
}