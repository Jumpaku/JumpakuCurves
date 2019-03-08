package jumpaku.curves.core.test.geom

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.geom.weighted
import jumpaku.commons.json.parseJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class WeightedPointTest {

    @Test
    fun testToString() {
        println("ToString")
        val wp = WeightedPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0), -0.4)
        assertThat(wp.toString().parseJson().tryMap { WeightedPoint.fromJson(it) }.orThrow(), `is`(closeTo(wp)))
    }

    @Test
    fun testDivide() {
        println("Divide")
        val p1 = WeightedPoint(Point.xr(2.0, 10.0), 3.0)
        val p2 = WeightedPoint(Point.xr(-2.0, 20.0), 2.0)
        assertThat(p1.lerp(-1.0, p2), `is`(closeTo(WeightedPoint(Point.xr(16.0 / 4.0, 100.0 / 4.0), 4.0))))
        assertThat(p1.lerp(0.0, p2), `is`(closeTo(WeightedPoint(Point.xr(6.0 / 3.0, 30.0 / 3.0), 3.0))))
        assertThat(p1.lerp(0.4, p2), `is`(closeTo(WeightedPoint(Point.xr(2.0 / 2.6, 34.0 / 2.6), 2.6))))
        assertThat(p1.lerp(1.0, p2), `is`(closeTo(WeightedPoint(Point.xr(-4.0 / 2.0, 40.0 / 2.0), 2.0))))
        assertThat(p1.lerp(2.0, p2), `is`(closeTo(WeightedPoint(Point.xr(-14.0 / 1.0, 110.0 / 1.0), 1.0))))
    }

    @Test
    fun testWeighted() {
        println("Point.weighted")
        val a = Point.xyzr(1.0, 2.0, 3.0, 4.0).weighted(-0.4)
        val e = WeightedPoint(Point.xyzr(1.0, 2.0, 3.0, 4.0), -0.4)
        assertThat(a, `is`(closeTo(e)))
    }
}