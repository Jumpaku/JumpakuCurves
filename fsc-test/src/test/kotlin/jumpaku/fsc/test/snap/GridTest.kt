package jumpaku.fsc.test.snap

import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.json.parseJson
import jumpaku.core.test.geom.shouldEqualToPoint
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import org.amshove.kluent.shouldEqual
import org.apache.commons.math3.util.FastMath
import org.junit.Test


class GridTest {

    val p2 = FastMath.PI/2

    val baseGrid = Grid(
            baseSpacing = 4.0,
            magnification = 2,
            origin = Point.xyz(4.0, 4.0, 0.0),
            rotation = Rotate(Vector.K, p2),
            baseFuzziness = 2.0)

    @Test
    fun testLocalToWorld() {
        println("LocalToWorld")
        baseGrid.localToWorld(0)(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        baseGrid.localToWorld(0)(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 8.0))
        baseGrid.localToWorld(0)(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(0.0, 4.0))

        baseGrid.localToWorld(-1)(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        baseGrid.localToWorld(-1)(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 12.0))
        baseGrid.localToWorld(-1)(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(-4.0, 4.0))

        baseGrid.localToWorld(1)(Point.xy(0.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 4.0))
        baseGrid.localToWorld(1)(Point.xy(1.0, 0.0)).shouldEqualToPoint(Point.xy(4.0, 6.0))
        baseGrid.localToWorld(1)(Point.xy(0.0, 1.0)).shouldEqualToPoint(Point.xy(2.0, 4.0))
    }

    @Test
    fun testSnapToNearestGrid() {
        println("SnapToNearestGrid")
        baseGrid.snapToNearestGrid(Point.xy(0.0, 0.0), 0).shouldEqual(GridPoint(-1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(1.0, 0.0), 0).shouldEqual(GridPoint(-1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(2.0, 0.0), 0).shouldEqual(GridPoint( -1, 1, 0))
        baseGrid.snapToNearestGrid(Point.xy(3.0, 0.0), 0).shouldEqual(GridPoint( -1, 0, 0))
        baseGrid.snapToNearestGrid(Point.xy(4.0, 0.0), 0).shouldEqual(GridPoint( -1, 0, 0))
    }

    @Test
    fun testToString() {
        println("ToString")
        baseGrid.toString().parseJson().tryFlatMap { Grid.fromJson(it) }.orThrow().shouldEqualToGrid(baseGrid)
    }
}


