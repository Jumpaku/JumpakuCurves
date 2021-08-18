package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Calibrate
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class CalibrateTest {

    val p0 = Point(1.0, -2.0, 3.0) to Point(-4.0, 5.0, -6.0)
    val p1 = Point(-1.0, 2.0, -3.0) to Point(4.0, 5.0, 6.0)
    val p2 = Point(-1.0, -2.0, -3.0) to Point(3.0, 2.0, 1.0)
    val p3 = Point(1.0, 2.0, 33.0) to Point(-3.0, -2.0, -1.0)

    val t4 = Calibrate(p0, p1, p2, p3)
    val t3 = Calibrate(p0, p1, p2)
    val t2 = Calibrate(p0, p1)
    val t1 = Calibrate(p0)

    @Test
    fun testInvoke4() {
        println("Invoke4")
        assertThat(t4(p0.first), `is`(closeTo(p0.second)))
        assertThat(t4(p1.first), `is`(closeTo(p1.second)))
        assertThat(t4(p2.first), `is`(closeTo(p2.second)))
        assertThat(t4(p3.first), `is`(closeTo(p3.second)))
    }

    @Test
    fun testInvoke3() {
        println("Invoke3")
        assertThat(t3(p0.first), `is`(closeTo(p0.second)))
        assertThat(t3(p1.first), `is`(closeTo(p1.second)))
        assertThat(t3(p2.first), `is`(closeTo(p2.second)))
    }

    @Test
    fun testInvoke2() {
        println("Invoke2")
        assertThat(t2(p0.first), `is`(closeTo(p0.second)))
        assertThat(t2(p1.first), `is`(closeTo(p1.second)))
    }

    @Test
    fun testInvoke1() {
        println("Invoke1")
        assertThat(t1(p0.first), `is`(closeTo(p0.second)))
    }

    @Test
    fun testDegeneration() {
        println("Degeneration")
        val t = Calibrate(p0, p0, p1, p1)
        assertThat(t(p0.first), `is`(closeTo(p0.second)))
        assertThat(t(p1.first), `is`(closeTo(p1.second)))
    }

    @Test
    fun testSimilarityWithNormal() {
        println("SimilarityWithNormal")
        val p0 = Point.xyz(0.0, 0.0, 0.0) to Point.xyz(0.0, 1.0, 1.0)
        val p1 = Point.xyz(1.0, 0.0, 0.0) to Point.xyz(0.0, -1.0, 1.0)
        val p2 = Point.xyz(1.0, 1.0, 0.0) to Point.xyz(0.0, -1.0, -1.0)
        val p3 = Point.xyz(0.0, 1.0, 0.0) to Point.xyz(0.0, 1.0, -1.0)
        val p4 = Point.xyz(0.5, 0.5, 0.0) to Point.xyz(0.0, 0.0, 0.0)
        val n = Vector(0.0, 0.0, 1.0) to Vector(1.0, 0.0, 0.0)
        val t = Calibrate.similarityWithNormal(p0, p1, n)
        println(t.matrix)
        assertThat(t(p0.first), `is`(closeTo(p0.second)))
        assertThat(t(p1.first), `is`(closeTo(p1.second)))
        assertThat(t(p2.first), `is`(closeTo(p2.second)))
        assertThat(t(p3.first), `is`(closeTo(p3.second)))
        assertThat(t(p4.first), `is`(closeTo(p4.second)))
    }
}