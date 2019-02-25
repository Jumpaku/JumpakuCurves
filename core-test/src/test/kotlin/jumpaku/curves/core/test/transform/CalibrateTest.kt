package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import org.junit.jupiter.api.Test

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
        t4(p0.first).shouldEqualToPoint(p0.second)
        t4(p1.first).shouldEqualToPoint(p1.second)
        t4(p2.first).shouldEqualToPoint(p2.second)
        t4(p3.first).shouldEqualToPoint(p3.second)
    }
    @Test
    fun testInvoke3() {
        println("Invoke3")
        t3(p0.first).shouldEqualToPoint(p0.second)
        t3(p1.first).shouldEqualToPoint(p1.second)
        t3(p2.first).shouldEqualToPoint(p2.second)
    }
    @Test
    fun testInvoke2() {
        println("Invoke2")
        t2(p0.first).shouldEqualToPoint(p0.second)
        t2(p1.first).shouldEqualToPoint(p1.second)
    }
    @Test
    fun testInvoke1() {
        println("Invoke1")
        t1(p0.first).shouldEqualToPoint(p0.second)
    }
}