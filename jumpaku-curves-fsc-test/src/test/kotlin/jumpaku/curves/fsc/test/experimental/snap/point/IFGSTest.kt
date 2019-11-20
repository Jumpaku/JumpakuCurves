package jumpaku.curves.fsc.test.experimental.snap.point

import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.experimental.snap.point.IFGS
import jumpaku.curves.fsc.snap.Grid
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class IFGSTest {

    val baseGrid = Grid(
            baseSpacing = 2.0,
            magnification = 2,
            origin = Point.xyz(0.0, 0.0, 0.0),
            rotation = Rotate(Vector.K, 0.0),
            baseFuzziness = 0.5)

    val snapper = IFGS

    fun assertSnapping(cursor: Point, expected: Option<Point>) {
        val result = snapper.snap(baseGrid, cursor)
        val actual = result.map { it.worldPoint(baseGrid).copy(r = baseGrid.fuzziness(it.resolution)) }
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
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.125)))
        assertSnapping(xr[1], Some(Point.xr(-7 / 8.0, 0.03125)))
        assertSnapping(xr[2], Some(Point.xr(-6 / 8.0, 0.0625)))
        assertSnapping(xr[3], Some(Point.xr(-5 / 8.0, 0.03125)))
        assertSnapping(xr[4], Some(Point.xr(-4 / 8.0, 0.125)))
        assertSnapping(xr[5], Some(Point.xr(-3 / 8.0, 0.03125)))
        assertSnapping(xr[6], Some(Point.xr(-2 / 8.0, 0.0625)))
        assertSnapping(xr[7], Some(Point.xr(-1 / 8.0, 0.03125)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 0.125)))
        assertSnapping(xr[9], Some(Point.xr(1 / 8.0, 0.03125)))
        assertSnapping(xr[10], Some(Point.xr(2 / 8.0, 0.0625)))
        assertSnapping(xr[11], Some(Point.xr(3 / 8.0, 0.03125)))
        assertSnapping(xr[12], Some(Point.xr(4 / 8.0, 0.125)))
        assertSnapping(xr[13], Some(Point.xr(5 / 8.0, 0.03125)))
        assertSnapping(xr[14], Some(Point.xr(6 / 8.0, 0.0625)))
        assertSnapping(xr[15], Some(Point.xr(7 / 8.0, 0.03125)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.125)))
        assertSnapping(xr[17], Some(Point.xr(9 / 8.0, 0.03125)))
        assertSnapping(xr[18], Some(Point.xr(10 / 8.0, 0.0625)))
        assertSnapping(xr[19], Some(Point.xr(11 / 8.0, 0.03125)))
        assertSnapping(xr[20], Some(Point.xr(12 / 8.0, 0.125)))
        assertSnapping(xr[21], Some(Point.xr(13 / 8.0, 0.03125)))
        assertSnapping(xr[22], Some(Point.xr(14 / 8.0, 0.0625)))
        assertSnapping(xr[23], Some(Point.xr(15 / 8.0, 0.03125)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.125)))
        assertSnapping(xr[25], Some(Point.xr(17 / 8.0, 0.03125)))
        assertSnapping(xr[26], Some(Point.xr(18 / 8.0, 0.0625)))
        assertSnapping(xr[27], Some(Point.xr(19 / 8.0, 0.03125)))
        assertSnapping(xr[28], Some(Point.xr(20 / 8.0, 0.125)))
        assertSnapping(xr[29], Some(Point.xr(21 / 8.0, 0.03125)))
        assertSnapping(xr[30], Some(Point.xr(22 / 8.0, 0.0625)))
        assertSnapping(xr[31], Some(Point.xr(23 / 8.0, 0.03125)))
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.125)))
    }

    @Test
    fun testSnap1() {
        println("Snap1")
        val xr = makeCursor(0.25)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], Some(Point.xr(-7 / 8.0, 0.03125)))
        assertSnapping(xr[2], Some(Point.xr(-6 / 8.0, 0.0625)))
        assertSnapping(xr[3], Some(Point.xr(-5 / 8.0, 0.03125)))
        assertSnapping(xr[4], Some(Point.xr(-4 / 8.0, 0.125)))
        assertSnapping(xr[5], Some(Point.xr(-3 / 8.0, 0.03125)))
        assertSnapping(xr[6], Some(Point.xr(-2 / 8.0, 0.0625)))
        assertSnapping(xr[7], Some(Point.xr(-1 / 8.0, 0.03125)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[9], Some(Point.xr(1 / 8.0, 0.03125)))
        assertSnapping(xr[10], Some(Point.xr(2 / 8.0, 0.0625)))
        assertSnapping(xr[11], Some(Point.xr(3 / 8.0, 0.03125)))
        assertSnapping(xr[12], Some(Point.xr(4 / 8.0, 0.125)))
        assertSnapping(xr[13], Some(Point.xr(5 / 8.0, 0.03125)))
        assertSnapping(xr[14], Some(Point.xr(6 / 8.0, 0.0625)))
        assertSnapping(xr[15], Some(Point.xr(7 / 8.0, 0.03125)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(9 / 8.0, 0.03125)))
        assertSnapping(xr[18], Some(Point.xr(10 / 8.0, 0.0625)))
        assertSnapping(xr[19], Some(Point.xr(11 / 8.0, 0.03125)))
        assertSnapping(xr[20], Some(Point.xr(12 / 8.0, 0.125)))
        assertSnapping(xr[21], Some(Point.xr(13 / 8.0, 0.03125)))
        assertSnapping(xr[22], Some(Point.xr(14 / 8.0, 0.0625)))
        assertSnapping(xr[23], Some(Point.xr(15 / 8.0, 0.03125)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[25], Some(Point.xr(17 / 8.0, 0.03125)))
        assertSnapping(xr[26], Some(Point.xr(18 / 8.0, 0.0625)))
        assertSnapping(xr[27], Some(Point.xr(19 / 8.0, 0.03125)))
        assertSnapping(xr[28], Some(Point.xr(20 / 8.0, 0.125)))
        assertSnapping(xr[29], Some(Point.xr(21 / 8.0, 0.03125)))
        assertSnapping(xr[30], Some(Point.xr(22 / 8.0, 0.0625)))
        assertSnapping(xr[31], Some(Point.xr(23 / 8.0, 0.03125)))
        assertSnapping(xr[32], Some(Point.xr(24 / 8.0, 0.25)))
    }

    @Test
    fun testSnap2() {
        println("Snap2")
        val xr = makeCursor(0.5)
        assertSnapping(xr[0], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[1], Some(Point.xr(-8 / 8.0, 0.25)))
        assertSnapping(xr[2], Some(Point.xr(-6 / 8.0, 0.0625)))
        assertSnapping(xr[3], Some(Point.xr(-4 / 8.0, 0.0625)))
        assertSnapping(xr[4], Some(Point.xr(-4 / 8.0, 0.125)))
        assertSnapping(xr[5], Some(Point.xr(-4 / 8.0, 0.0625)))
        assertSnapping(xr[6], Some(Point.xr(-2 / 8.0, 0.0625)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[10], Some(Point.xr(2 / 8.0, 0.0625)))
        assertSnapping(xr[11], Some(Point.xr(4 / 8.0, 0.0625)))
        assertSnapping(xr[12], Some(Point.xr(4 / 8.0, 0.125)))
        assertSnapping(xr[13], Some(Point.xr(4 / 8.0, 0.0625)))
        assertSnapping(xr[14], Some(Point.xr(6 / 8.0, 0.0625)))
        assertSnapping(xr[15], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[18], Some(Point.xr(10 / 8.0, 0.0625)))
        assertSnapping(xr[19], Some(Point.xr(12 / 8.0, 0.0625)))
        assertSnapping(xr[20], Some(Point.xr(12 / 8.0, 0.125)))
        assertSnapping(xr[21], Some(Point.xr(12 / 8.0, 0.0625)))
        assertSnapping(xr[22], Some(Point.xr(14 / 8.0, 0.0625)))
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[26], Some(Point.xr(18 / 8.0, 0.0625)))
        assertSnapping(xr[27], Some(Point.xr(20 / 8.0, 0.0625)))
        assertSnapping(xr[28], Some(Point.xr(20 / 8.0, 0.125)))
        assertSnapping(xr[29], Some(Point.xr(20 / 8.0, 0.0625)))
        assertSnapping(xr[30], Some(Point.xr(22 / 8.0, 0.0625)))
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
        assertSnapping(xr[4], Some(Point.xr(-4 / 8.0, 0.125)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 1.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 0.5)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 0.25)))
        assertSnapping(xr[12], Some(Point.xr(4 / 8.0, 0.125)))
        assertSnapping(xr[13], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[14], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[15], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[16], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[17], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[18], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[19], Some(Point.xr(8 / 8.0, 0.25)))
        assertSnapping(xr[20], Some(Point.xr(12 / 8.0, 0.125)))
        assertSnapping(xr[21], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[22], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[23], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[24], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[25], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[26], Some(Point.xr(16 / 8.0, 0.5)))
        assertSnapping(xr[27], Some(Point.xr(16 / 8.0, 0.25)))
        assertSnapping(xr[28], Some(Point.xr(20 / 8.0, 0.125)))
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
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 2.0)))
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
        assertSnapping(xr[0], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[1], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[2], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[3], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[4], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[12], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[13], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[14], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[15], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[16], Some(Point.xr(0 / 8.0, 2.0)))
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
        assertSnapping(xr[32], Some(Point.xr(32 / 8.0, 2.0)))
    }

    @Test
    fun testSnap6() {
        println("Snap6")
        val xr = makeCursor(8.0)
        assertSnapping(xr[0], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[1], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[2], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[3], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[4], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[5], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[6], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[7], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[8], Some(Point.xr(0 / 8.0, 8.0)))
        assertSnapping(xr[9], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[10], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[11], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[12], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[13], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[14], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[15], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[16], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[17], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[18], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[19], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[20], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[21], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[22], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[23], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[24], Some(Point.xr(0 / 8.0, 4.0)))
        assertSnapping(xr[25], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[26], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[27], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[28], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[29], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[30], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[31], Some(Point.xr(0 / 8.0, 2.0)))
        assertSnapping(xr[32], Some(Point.xr(0 / 8.0, 2.0)))
    }
}