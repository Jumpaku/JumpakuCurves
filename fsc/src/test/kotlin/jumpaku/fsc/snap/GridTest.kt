package jumpaku.fsc.snap

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.affine.rotation
import jumpaku.core.json.parseToJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

class GridTest {

    val baseGrid = BaseGrid(
            baseGridSpacing = 4.0,
            magnification = 4,
            origin = Point.xyz(4.0, 4.0, 0.0),
            rotation = rotation(Vector(0.0, 0.0, 1.0), FastMath.PI/2),
            fuzziness = 2.0)
    val higherGrid = DerivedGrid(baseGrid, 1)
    val lowerGrid = DerivedGrid(baseGrid, -1)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(baseGrid.resolution).isEqualTo(0)

        assertThat(higherGrid.baseGridSpacing).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(higherGrid.magnification).isEqualTo(4)
        pointAssertThat(higherGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        pointAssertThat(higherGrid.rotation(Point.xy(1.0, 0.0))).isEqualToPoint(Point.xy(0.0, 1.0))
        assertThat(higherGrid.fuzziness).isEqualTo(0.5, withPrecision(1.0e-10))
        assertThat(higherGrid.resolution).isEqualTo(1)

        assertThat(lowerGrid.baseGridSpacing).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.magnification).isEqualTo(4)
        pointAssertThat(lowerGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        pointAssertThat(lowerGrid.rotation(Point.xy(1.0, 0.0))).isEqualToPoint(Point.xy(0.0, 1.0))
        assertThat(lowerGrid.fuzziness).isEqualTo(8.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.resolution).isEqualTo(-1)
    }

    @Test
    fun testToString() {
        println("ToString")
        val j = baseGrid.toString().parseToJson().get().grid
        gridAssertThat(j).isEqualToGrid(baseGrid)
    }

    @Test
    fun testDeriveGrid() {
        println("DeriveGrid")
        val b = baseGrid.deriveGrid(0)
        val h = baseGrid.deriveGrid(1)
        val l = baseGrid.deriveGrid(-1)

        gridAssertThat(b).isEqualToGrid(baseGrid)
        gridAssertThat(h).isEqualToGrid(higherGrid)
        gridAssertThat(l).isEqualToGrid(lowerGrid)
    }

    @Test
    fun testGetGridSpacing() {
        println("GetGridSpacing")
        assertThat(baseGrid.gridSpacing).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(higherGrid.gridSpacing).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.gridSpacing).isEqualTo(16.0, withPrecision(1.0e-10))
    }

    @Test
    fun testTransforms() {
        println("Transforms")
        pointAssertThat(baseGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(-16.0, 44.0))
        pointAssertThat(higherGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(-1.0, 14.0))
        pointAssertThat(lowerGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(-76.0, 164.0))
        pointAssertThat(baseGrid.worldToLocal(Point.xy(-16.0, 44.0))).isEqualToPoint(Point.xy(10.0, 5.0))
        pointAssertThat(higherGrid.worldToLocal(Point.xy(-1.0, 14.0))).isEqualToPoint(Point.xy(10.0, 5.0))
        pointAssertThat(lowerGrid.worldToLocal(Point.xy(-76.0, 164.0))).isEqualToPoint(Point.xy(10.0, 5.0))
    }

    @Test
    fun testSnap() {
        println("Snap")
        gridCoordinateAssertThat(baseGrid.snap(Point.xy(0.0, 0.0)))
                .isEqualToGridCoordinate(GridCoordinate(-1, 1, 0, baseGrid))
        gridCoordinateAssertThat(baseGrid.snap(Point.xy(1.0, 0.0)))
                .isEqualToGridCoordinate(GridCoordinate(-1, 1, 0, baseGrid))
        gridCoordinateAssertThat(baseGrid.snap(Point.xy(2.0, 0.0)))
                .isEqualToGridCoordinate(GridCoordinate(-1, 1, 0, baseGrid))
        gridCoordinateAssertThat(baseGrid.snap(Point.xy(3.0, 0.0)))
                .isEqualToGridCoordinate(GridCoordinate(-1, 0, 0, baseGrid))
        gridCoordinateAssertThat(baseGrid.snap(Point.xy(4.0, 0.0)))
                .isEqualToGridCoordinate(GridCoordinate(-1, 0, 0, baseGrid))
    }
}


fun gridAssertThat(actual: Grid): GridAssert = GridAssert(actual)

class GridAssert(actual: Grid) : AbstractAssert<GridAssert, Grid>(actual, GridAssert::class.java) {

    fun isEqualToGrid(expected: Grid, eps: Double = 1.0e-10): GridAssert {
        isNotNull

        assertThat(actual.baseGridSpacing).isEqualTo(expected.baseGridSpacing, withPrecision(eps))
        assertThat(actual.magnification).isEqualTo(expected.magnification)
        pointAssertThat(actual.origin).isEqualToPoint(expected.origin, eps)
        pointAssertThat(actual.rotation(Point.xyz(1.0, 2.0, -3.0)))
                .isEqualToPoint(expected.rotation(Point.xyz(1.0, 2.0, -3.0)), eps)
        assertThat(actual.fuzziness).isEqualTo(expected.fuzziness, withPrecision(eps))
        assertThat(actual.resolution).isEqualTo(expected.resolution)

        return this
    }
}
