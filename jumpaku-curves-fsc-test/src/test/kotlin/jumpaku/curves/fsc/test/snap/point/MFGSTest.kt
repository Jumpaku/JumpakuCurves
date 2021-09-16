package jumpaku.curves.fsc.test.snap.point

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.fsc.snap.point.transformToWorld
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.Assert.assertThat
import org.junit.Test


class MFGSTest {

    val baseGrid = Grid(
            baseSpacingInWorld = 2.0,
            magnification = 2,
            originInWorld = Point.xyz(0.0, 0.0, 0.0),
            rotationInWorld = Rotate(Vector.K, 0.0),
            baseFuzzinessInWorld = 0.5)

    val snapper = MFGS(-1, 1)

    fun assertSnapping(cursor: Point, expected: Option<Point>) {
        val result = snapper.snap(baseGrid, cursor)
        val actual = result.map { baseGrid.transformToWorld(it) }
        assertThat(actual is Some, `is`(expected is Some))
        if (actual is Some && expected is Some) {
            assertThat(expected.value.isNecessary(cursor), `is`(greaterThanOrEqualTo(Grade(0.5))))
        }
        result.forEach {
            assertThat(it.grade, `is`(greaterThanOrEqualTo(Grade(0.5))))
        }
    }

    fun makeCursor(r: Double): List<Point> = List(33) {
        Point.xr(-1 + it * 0.125, r)
    }
    
    @Test
    fun testSnap0() {
        println("Snap0")
        val xr = makeCursor(0.125)
        assertSnapping(xr[0], None)
        assertSnapping(xr[1], None)
        assertSnapping(xr[2], None)
        assertSnapping(xr[3], None)
        assertSnapping(xr[4], None)
        assertSnapping(xr[5], None)
        assertSnapping(xr[6], None)
        assertSnapping(xr[7], None)
        assertSnapping(xr[8], None)
        assertSnapping(xr[9], None)
        assertSnapping(xr[10], None)
        assertSnapping(xr[11], None)
        assertSnapping(xr[12], None)
        assertSnapping(xr[13], None)
        assertSnapping(xr[14], None)
        assertSnapping(xr[15], None)
        assertSnapping(xr[16], None)
        assertSnapping(xr[17], None)
        assertSnapping(xr[18], None)
        assertSnapping(xr[19], None)
        assertSnapping(xr[20], None)
        assertSnapping(xr[21], None)
        assertSnapping(xr[22], None)
        assertSnapping(xr[23], None)
        assertSnapping(xr[24], None)
        assertSnapping(xr[25], None)
        assertSnapping(xr[26], None)
        assertSnapping(xr[27], None)
        assertSnapping(xr[28], None)
        assertSnapping(xr[29], None)
        assertSnapping(xr[30], None)
        assertSnapping(xr[31], None)
        assertSnapping(xr[32], None)
    }

    @Test
    fun testSnap1() {
        println("Snap1")
        val xr = makeCursor(0.25)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], None)
        assertSnapping(xr[2], None)
        assertSnapping(xr[3], None)
        assertSnapping(xr[4], None)
        assertSnapping(xr[5], None)
        assertSnapping(xr[6], None)
        assertSnapping(xr[7], None)
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[9], None)
        assertSnapping(xr[10], None)
        assertSnapping(xr[11], None)
        assertSnapping(xr[12], None)
        assertSnapping(xr[13], None)
        assertSnapping(xr[14], None)
        assertSnapping(xr[15], None)
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], None)
        assertSnapping(xr[18], None)
        assertSnapping(xr[19], None)
        assertSnapping(xr[20], None)
        assertSnapping(xr[21], None)
        assertSnapping(xr[22], None)
        assertSnapping(xr[23], None)
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[25], None)
        assertSnapping(xr[26], None)
        assertSnapping(xr[27], None)
        assertSnapping(xr[28], None)
        assertSnapping(xr[29], None)
        assertSnapping(xr[30], None)
        assertSnapping(xr[31], None)
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.25)))
    }

    @Test
    fun testSnap2() {
        println("Snap2")
        val xr = makeCursor(0.5)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[2], None)
        assertSnapping(xr[3], None)
        assertSnapping(xr[4], None)
        assertSnapping(xr[5], None)
        assertSnapping(xr[6], None)
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[10], None)
        assertSnapping(xr[11], None)
        assertSnapping(xr[12], None)
        assertSnapping(xr[13], None)
        assertSnapping(xr[14], None)
        assertSnapping(xr[15], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[18], None)
        assertSnapping(xr[19], None)
        assertSnapping(xr[20], None)
        assertSnapping(xr[21], None)
        assertSnapping(xr[22], None)
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[26], None)
        assertSnapping(xr[27], None)
        assertSnapping(xr[28], None)
        assertSnapping(xr[29], None)
        assertSnapping(xr[30], None)
        assertSnapping(xr[31], Some(Point.xr(24 / 8.0, 0.25)))
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.25)))
    }

    @Test
    fun testSnap3() {
        println("Snap3")
        val xr = makeCursor(1.0)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[2], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[3], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[4], None)
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[12], None)
        assertSnapping(xr[13], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[14], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[15], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[18], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[19], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[20], None)
        assertSnapping(xr[21], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[22], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[26], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[27], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[28], None)
        assertSnapping(xr[29], Some(Point.xr(24 / 8.0, 0.25)))
        assertSnapping(xr[30], Some(Point.xr(24 / 8.0, 0.25)))
        assertSnapping(xr[31], Some(Point.xr(24 / 8.0, 0.25)))
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.25)))
    }

    @Test
    fun testSnap4() {
        println("Snap4")
        val xr = makeCursor(2.0)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[2], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[3], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[4], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[12], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[13], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[14], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[15], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[18], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[19], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[20], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[21], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[22], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[26], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[27], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[28], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[29], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[30], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[31], Some(Point.xr(24 / 8.0, 0.25)))
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.25)))
    }


    @Test
    fun testSnap5() {
        println("Snap5")
        val xr = makeCursor(4.0)
        assertSnapping(xr[0], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[1], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[2], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[3], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[4], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[12], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[13], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[14], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[15], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[16], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[17], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[18], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[19], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[20], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[21], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[22], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[26], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[27], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[28], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[29], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[30], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[31], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[32], Some(Point.xr(32 / 8.0, 1.0)))
    }

    @Test
    fun testSnap6() {
        println("Snap6")
        val xr = makeCursor(8.0)
        assertSnapping(xr[0], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[1], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[2], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[3], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[4], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[12], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[13], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[14], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[15], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[16], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[17], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[18], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[19], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[20], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[21], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[22], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[23], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[24], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[25], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[26], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[27], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[28], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[29], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[30], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[31], Some(Point.xr(32 / 8.0, 1.0)))
        assertSnapping(xr[32], Some(Point.xr(32 / 8.0, 1.0)))
    }
}