package jumpaku.fsc.snap

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.affine.rotation
import jumpaku.core.json.parseToJson
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.*
import org.junit.Test

class GridTest {

    val baseGrid = BaseGrid(
            spacing = 4.0,
            magnification = 4,
            origin = Point.xyz(4.0, 4.0, 0.0),
            fuzziness = 2.0)

    val higherGrid = DerivedGrid(baseGrid, 1)

    val lowerGrid = DerivedGrid(baseGrid, -1)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(baseGrid.resolution).isEqualTo(0)

        assertThat(higherGrid.spacing).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(higherGrid.magnification).isEqualTo(4)
        pointAssertThat(higherGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        assertThat(higherGrid.fuzziness).isEqualTo(0.5, withPrecision(1.0e-10))
        assertThat(higherGrid.resolution).isEqualTo(1)

        assertThat(lowerGrid.spacing).isEqualTo(16.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.magnification).isEqualTo(4)
        pointAssertThat(lowerGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        assertThat(lowerGrid.fuzziness).isEqualTo(8.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.resolution).isEqualTo(-1)
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
    fun testLocalToWorld() {
        println("LocalToWorld")
        pointAssertThat(baseGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(44.0, 24.0))
        pointAssertThat(higherGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(14.0, 9.0))
        pointAssertThat(lowerGrid.localToWorld(Point.xy(10.0, 5.0))).isEqualToPoint(Point.xy(164.0, 84.0))
    }

    @Test
    fun testSnap() {
        println("Snap")
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(0.0, 0.0)))
                .isEqualToGridPoint(GridPoint(-1, -1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(1.0, 0.0)))
                .isEqualToGridPoint(GridPoint(-1, -1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(2.0, 0.0)))
                .isEqualToGridPoint(GridPoint( 0, -1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(3.0, 0.0)))
                .isEqualToGridPoint(GridPoint( 0, -1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(4.0, 0.0)))
                .isEqualToGridPoint(GridPoint( 0, -1, 0))
    }
}


fun gridAssertThat(actual: Grid): GridAssert = GridAssert(actual)

class GridAssert(actual: Grid) : AbstractAssert<GridAssert, Grid>(actual, GridAssert::class.java) {

    fun isEqualToGrid(expected: Grid, eps: Double = 1.0e-10): GridAssert {
        isNotNull

        assertThat(actual.spacing).isEqualTo(expected.spacing, withPrecision(eps))
        assertThat(actual.magnification).isEqualTo(expected.magnification)
        pointAssertThat(actual.origin).isEqualToPoint(expected.origin, eps)
        assertThat(actual.fuzziness).isEqualTo(expected.fuzziness, withPrecision(eps))
        assertThat(actual.resolution).isEqualTo(expected.resolution)

        return this
    }
}
