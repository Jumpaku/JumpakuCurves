package jumpaku.curves.fsc.test.snap.point

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.point.PointSnapResult
import jumpaku.curves.fsc.snap.point.transformToWorld
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PointSnapResultTest {

    val baseGrid = Grid(
            baseSpacingInWorld = 4.0,
            magnification = 2,
            originInWorld = Point.xyz(4.0, 4.0, 0.0),
            rotationInWorld = Rotate(Vector.K, FastMath.PI / 2),
            baseFuzzinessInWorld = 2.0)


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

