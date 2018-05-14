package jumpaku.fsc.test.snap

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.shouldEqualToPoint
import jumpaku.core.test.affine.shouldEqualToVector
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
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
        baseGrid.resolution.shouldEqualTo(0)
        baseGrid.isNoGrid.shouldBeFalse()

        higherGrid.spacing.shouldBeCloseTo(2.0)
        higherGrid.magnification.shouldEqualTo(2)
        higherGrid.origin.shouldEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        higherGrid.axis.shouldEqualToVector(Vector.K)
        higherGrid.radian.shouldBeCloseTo(p2)
        higherGrid.fuzziness.shouldBeCloseTo(1.0)
        higherGrid.resolution.shouldEqualTo(1)
        higherGrid.isNoGrid.shouldBeFalse()

        lowerGrid.spacing.shouldBeCloseTo(8.0)
        lowerGrid.magnification.shouldEqualTo(2)
        lowerGrid.origin.shouldEqualToPoint(Point.xyz(4.0, 4.0, 0.0))
        lowerGrid.axis.shouldEqualToVector(Vector.K)
        lowerGrid.radian.shouldBeCloseTo(p2)
        lowerGrid.fuzziness.shouldBeCloseTo(4.0)
        lowerGrid.resolution.shouldEqualTo(-1)
        lowerGrid.isNoGrid.shouldBeFalse()

        Grid.noGrid(baseGrid).isNoGrid.shouldBeTrue()
    }

    @Test
    fun testDeriveGrid() {
        println("DeriveGrid")
        baseGrid.deriveGrid(0).shouldEqualToGrid(baseGrid)
        baseGrid.deriveGrid(1).shouldEqualToGrid(higherGrid)
        baseGrid.deriveGrid(-1).shouldEqualToGrid(lowerGrid)
    }

    @Test
    fun testLocalToWorld() {
        println("LocalToWorld")
        baseGrid.localToWorld(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        baseGrid.localToWorld(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 8.0))
        baseGrid.localToWorld(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(0.0, 4.0))

        lowerGrid.localToWorld(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        lowerGrid.localToWorld(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 12.0))
        lowerGrid.localToWorld(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(-4.0, 4.0))

        higherGrid.localToWorld(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        higherGrid.localToWorld(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 6.0))
        higherGrid.localToWorld(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(2.0, 4.0))
    }

    @Test
    fun testSnapToNearestGrid() {
        println("SnapToNearestGrid")
        baseGrid.snapToNearestGrid(Point.xy(0.0, 0.0)).shouldEqual(GridPoint(-1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(1.0, 0.0)).shouldEqual(GridPoint(-1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(2.0, 0.0)).shouldEqual(GridPoint( -1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(3.0, 0.0)).shouldEqual(GridPoint( -1, 0, 0))
        baseGrid.snapToNearestGrid(Point.xy(4.0, 0.0)).shouldEqual(GridPoint( -1, 0, 0))
    }

    @Test
    fun testToString() {
        println("ToString")
        baseGrid.toString().parseJson().flatMap { Grid.fromJson(it) }.get().shouldEqualToGrid(baseGrid)
    }
}


