package jumpaku.fsc.snap

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.affine.rotation
import org.apache.commons.math3.util.FastMath
import org.assertj.core.api.Assertions.*
import org.junit.Test

class PointSnapperTest {

    private val baseGrid = BaseGrid(
            baseGridSpacing = 1.0,
            magnification = 4,
            origin = Point.xyz(0.0, 0.0, 0.0),
            rotation = rotation(Vector(0.0, 0.0, 1.0), 0.0),
            fuzziness = 0.25)

    private val snapper = PointSnapper(baseGrid, -1, 1)

    @Test
    fun testSnap0() {
        println("Snap0")
        val r = 1 / 32.0
        pointAssertThat(snapper.snap(Point.xr(0 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(0 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(1 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(1 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(2 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(2 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(3 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(3 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(4 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(4 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(5 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(5 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(6 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(6 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(7 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(7 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(8 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(8 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(9 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(9 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(10 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(10 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(11 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(11 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(12 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(12 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(13 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(13 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(14 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(14 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(15 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(15 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(16 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(16 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(17 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(17 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(18 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(18 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(19 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(19 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(20 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(20 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(21 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(21 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(22 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(22 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(23 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(23 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(24 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(24 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(25 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(25 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(26 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(26 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(27 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(27 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(28 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(28 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(29 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(29 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(30 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(30 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(31 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(31 / 32.0))
        pointAssertThat(snapper.snap(Point.xr(32 / 32.0, r)).snappedPoint).isEqualToPoint(Point.x(32 / 32.0))

        assertThat(snapper.snap(Point.xr(0 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(1 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(2 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(3 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(4 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(5 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(6 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(7 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(8 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(9 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(10 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(11 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(12 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(13 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(14 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(15 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(16 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(17 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(18 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(19 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(20 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(21 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(22 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(23 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(24 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(25 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(26 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(27 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(28 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(29 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(30 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(31 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(32 / 32.0, r)).snappedGridCoordinate.isDefined).isFalse()
    }

    @Test
    fun testSnap1() {
        println("Snap1")
        val r = 1/16.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 1/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 2/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 3/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 4/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 5/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 6/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 7/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 9/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x(10/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x(10/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x(12/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x(13/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x(14/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x(15/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(17/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(18/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(19/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(20/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(21/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(22/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(23/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(25/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(26/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(27/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(28/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(29/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(30/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(31/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }

    @Test
    fun testSnap2() {
        println("Snap2")
        val r = 1/8.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 2/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 3/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 4/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 5/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 6/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x(10/32.0))
        pointAssertThat(snapper.snap(Point.xr(11/32.0, r)).snappedPoint).isEqualToPoint(Point.x(11/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x(12/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x(13/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x(14/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(18/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(19/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(20/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(21/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(22/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(26/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(27/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(28/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(29/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(30/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(11/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }

    @Test
    fun testSnap3() {
        println("Snap3")
        val r = 1/4.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 4/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(11/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x(12/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(20/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(28/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isFalse()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }

    @Test
    fun testSnap4() {
        println("Snap4")
        val r = 1/2.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(11/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 8/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(24/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }

    @Test
    fun testSnap5() {
        println("Snap5")
        val r = 1.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(11/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(16/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }

    @Test
    fun testSnap6() {
        println("Snap6")
        val r = 2.0
        pointAssertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(10/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(11/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(12/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr(13/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(14/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(15/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))
        pointAssertThat(snapper.snap(Point.xr(16/32.0, r)).snappedPoint).isEqualToPoint(Point.x( 0/32.0))//
        pointAssertThat(snapper.snap(Point.xr(17/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(18/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(19/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(20/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(21/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(22/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(23/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(24/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(25/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(26/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(27/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(28/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//
        pointAssertThat(snapper.snap(Point.xr(29/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(30/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(31/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))
        pointAssertThat(snapper.snap(Point.xr(32/32.0, r)).snappedPoint).isEqualToPoint(Point.x(32/32.0))//

        assertThat(snapper.snap(Point.xr( 0/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 1/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 2/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 3/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 4/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 5/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 6/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 7/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 8/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr( 9/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(10/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(12/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(13/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(14/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(15/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(16/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(17/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(18/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(19/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(20/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(21/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(22/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(23/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(24/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(25/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(26/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(27/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(28/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(29/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(30/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(31/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
        assertThat(snapper.snap(Point.xr(32/32.0, r)).snappedGridCoordinate.isDefined).isTrue()
    }
}