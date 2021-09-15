package jumpaku.curves.fsc.test.snap

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.test.transform.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.SimilarityTransform
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test


class GridTest {

    val p2 = FastMath.PI / 2

    val baseGrid = Grid(
        baseSpacingInWorld = 4.0,
        magnification = 2,
        originInWorld = Point.xyz(4.0, 4.0, 0.0),
        rotationInWorld = Rotate(Vector.K, p2),
        baseFuzzinessInWorld = 2.0
    )

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(baseGrid.baseFuzzinessInWorld, `is`(closeTo(2.0)))
        assertThat(baseGrid.baseSpacingInWorld, `is`(closeTo(4.0)))
        assertThat(baseGrid.originInWorld, `is`(closeTo(Point.xyz(4.0, 4.0, 0.0))))
        assertThat(baseGrid.magnification, `is`(2))
        val a = SimilarityTransform.Identity
            .andThen(Rotate(Vector.K, p2))
            .andThen(UniformlyScale(4.0)).andThen(Translate(Vector(4.0, 4.0, 0.0)))
        assertThat(baseGrid.baseGridToWorld, `is`(closeTo(a)))
    }

    @Test
    fun testGridToWorld() {
        println("GridToWorld")
        assertThat(baseGrid.gridToWorld(0)(Point.xy(0.0, 0.0)), `is`(closeTo(Point.xy(4.0, 4.0))))
        assertThat(baseGrid.gridToWorld(0)(Point.xy(1.0, 0.0)), `is`(closeTo(Point.xy(4.0, 8.0))))
        assertThat(baseGrid.gridToWorld(0)(Point.xy(0.0, 1.0)), `is`(closeTo(Point.xy(0.0, 4.0))))

        assertThat(baseGrid.gridToWorld(-1)(Point.xy(0.0, 0.0)), `is`(closeTo(Point.xy(4.0, 4.0))))
        assertThat(baseGrid.gridToWorld(-1)(Point.xy(1.0, 0.0)), `is`(closeTo(Point.xy(4.0, 12.0))))
        assertThat(baseGrid.gridToWorld(-1)(Point.xy(0.0, 1.0)), `is`(closeTo(Point.xy(-4.0, 4.0))))

        assertThat(baseGrid.gridToWorld(1)(Point.xy(0.0, 0.0)), `is`(closeTo(Point.xy(4.0, 4.0))))
        assertThat(baseGrid.gridToWorld(1)(Point.xy(1.0, 0.0)), `is`(closeTo(Point.xy(4.0, 6.0))))
        assertThat(baseGrid.gridToWorld(1)(Point.xy(0.0, 1.0)), `is`(closeTo(Point.xy(2.0, 4.0))))
    }

    @Test
    fun testTransformToWorld() {
        println("LocalToWorld")
        assertThat(baseGrid.transformToWorld(GridPoint(0, 0, 0), 0), `is`(closeTo(Point.xyr(4.0, 4.0, 2.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(1, 0, 0), 0), `is`(closeTo(Point.xyr(4.0, 8.0, 2.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(0, 1, 0), 0), `is`(closeTo(Point.xyr(0.0, 4.0, 2.0))))

        assertThat(baseGrid.transformToWorld(GridPoint(0, 0, 0), -1), `is`(closeTo(Point.xyr(4.0, 4.0, 4.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(1, 0, 0), -1), `is`(closeTo(Point.xyr(4.0, 12.0, 4.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(0, 1, 0), -1), `is`(closeTo(Point.xyr(-4.0, 4.0, 4.0))))

        assertThat(baseGrid.transformToWorld(GridPoint(0, 0, 0), 1), `is`(closeTo(Point.xyr(4.0, 4.0, 1.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(1, 0, 0), 1), `is`(closeTo(Point.xyr(4.0, 6.0, 1.0))))
        assertThat(baseGrid.transformToWorld(GridPoint(0, 1, 0), 1), `is`(closeTo(Point.xyr(2.0, 4.0, 1.0))))
    }

    @Test
    fun testSnapToNearestGrid() {
        println("SnapToNearestGrid")
        assertThat(baseGrid.snapToNearestGrid(Point.xy(0.0, 0.0), 0), `is`(equalTo(GridPoint(-1, 1, 0))))
        assertThat(baseGrid.snapToNearestGrid(Point.xy(1.0, 0.0), 0), `is`(equalTo(GridPoint(-1, 1, 0))))
        assertThat(baseGrid.snapToNearestGrid(Point.xy(2.0, 0.0), 0), `is`(equalTo(GridPoint(-1, 1, 0))))
        assertThat(baseGrid.snapToNearestGrid(Point.xy(3.0, 0.0), 0), `is`(equalTo(GridPoint(-1, 0, 0))))
        assertThat(baseGrid.snapToNearestGrid(Point.xy(4.0, 0.0), 0), `is`(equalTo(GridPoint(-1, 0, 0))))
    }

    @Test
    fun testSimilarityTransform(){
        println("SimilarityTransform")
        val t = baseGrid.baseGridToWorld.invert().orThrow()
        val a = baseGrid.similarlyTransform(t)
        assertThat(a.baseFuzzinessInWorld, `is`(closeTo(0.5)))
        assertThat(a.baseSpacingInWorld, `is`(closeTo(1.0)))
        assertThat(a.originInWorld, `is`(closeTo(Point.origin)))
        assertThat(a.magnification, `is`(2))
        val e = SimilarityTransform.Identity
        assertThat(a.baseGridToWorld, `is`(closeTo(e)))
    }

    @Test
    fun testWorldToBaseGrid(){
        println("WorldToBaseGrid")
        val a = baseGrid.worldToBaseGrid
        val e = baseGrid.baseGridToWorld.invert().orThrow()
        assertThat(a, `is`(closeTo(e)))
    }
}


