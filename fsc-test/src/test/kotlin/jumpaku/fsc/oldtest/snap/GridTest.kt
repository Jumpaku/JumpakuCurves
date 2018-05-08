package jumpaku.fsc.oldtest.snap

import jumpaku.core.affine.*
import jumpaku.core.json.parseJson
import jumpaku.core.testold.affine.pointAssertThat
import jumpaku.core.testold.affine.vectorAssertThat
import jumpaku.fsc.oldtest.snap.gridAssertThat
import jumpaku.fsc.oldtest.snap.gridPointAssertThat
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.junit.Test

class GridTest {

    val p2 = FastMath.PI/2

    val baseGrid = Grid(
            spacing = 4.0,
            magnification = 2,
            origin = Point.xyz(4.0, 4.0, 0.0),
            axis = Vector.K,
            radian = p2,
            fuzziness = 2.0,
            resolution = 0)

    val higherGrid = baseGrid.deriveGrid(1)

    val lowerGrid = baseGrid.deriveGrid(-1)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(baseGrid.resolution).isEqualTo(0)
        assertThat(baseGrid.isNoGrid).isFalse()

        assertThat(higherGrid.spacing).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(higherGrid.magnification).isEqualTo(2)
        pointAssertThat(higherGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        vectorAssertThat(higherGrid.axis).isEqualToVector(Vector.K)
        assertThat(higherGrid.radian).isEqualTo(p2, withPrecision(1.0e-10))
        assertThat(higherGrid.fuzziness).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(higherGrid.resolution).isEqualTo(1)
        assertThat(higherGrid.isNoGrid).isFalse()

        assertThat(lowerGrid.spacing).isEqualTo(8.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.magnification).isEqualTo(2)
        pointAssertThat(lowerGrid.origin).isEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        vectorAssertThat(lowerGrid.axis).isEqualToVector(Vector.K)
        assertThat(lowerGrid.radian).isEqualTo(p2, withPrecision(1.0e-10))
        assertThat(lowerGrid.fuzziness).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(lowerGrid.resolution).isEqualTo(-1)
        assertThat(lowerGrid.isNoGrid).isFalse()

        assertThat(Grid.noGrid(baseGrid).isNoGrid).isTrue()
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
        pointAssertThat(baseGrid.localToWorld(Point.xy(0.0, 0.0))).isEqualToPoint(Point.xy(4.0, 4.0))
        pointAssertThat(baseGrid.localToWorld(Point.xy(1.0, 0.0))).isEqualToPoint(Point.xy(4.0, 8.0))
        pointAssertThat(baseGrid.localToWorld(Point.xy(0.0, 1.0))).isEqualToPoint(Point.xy(0.0, 4.0))

        pointAssertThat(lowerGrid.localToWorld(Point.xy(0.0, 0.0))).isEqualToPoint(Point.xy(4.0, 4.0))
        pointAssertThat(lowerGrid.localToWorld(Point.xy(1.0, 0.0))).isEqualToPoint(Point.xy(4.0, 12.0))
        pointAssertThat(lowerGrid.localToWorld(Point.xy(0.0, 1.0))).isEqualToPoint(Point.xy(-4.0, 4.0))

        pointAssertThat(higherGrid.localToWorld(Point.xy(0.0, 0.0))).isEqualToPoint(Point.xy(4.0, 4.0))
        pointAssertThat(higherGrid.localToWorld(Point.xy(1.0, 0.0))).isEqualToPoint(Point.xy(4.0, 6.0))
        pointAssertThat(higherGrid.localToWorld(Point.xy(0.0, 1.0))).isEqualToPoint(Point.xy(2.0, 4.0))
    }

    @Test
    fun testSnapToNearestGrid() {
        println("SnapToNearestGrid")
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(0.0, 0.0)))
                .isEqualToGridPoint(GridPoint(-1, 1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(1.0, 0.0)))
                .isEqualToGridPoint(GridPoint(-1, 1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(2.0, 0.0)))
                .isEqualToGridPoint(GridPoint( -1, 1, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(3.0, 0.0)))
                .isEqualToGridPoint(GridPoint( -1, 0, 0))
        gridPointAssertThat(baseGrid.snapToNearestGrid(Point.xy(4.0, 0.0)))
                .isEqualToGridPoint(GridPoint( -1, 0, 0))
    }

    @Test
    fun testToString() {
        println("ToString")
        gridAssertThat(baseGrid.toString().parseJson().flatMap { Grid.fromJson(it) }.get()).isEqualToGrid(baseGrid)
    }
}


