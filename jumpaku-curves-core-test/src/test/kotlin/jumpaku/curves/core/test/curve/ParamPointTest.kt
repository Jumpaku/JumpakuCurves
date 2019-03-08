package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.parseJson
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ParamPointTest {
    @Test
    fun testDivide() {
        println("Divide")
        val f0 = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        val f2 = ParamPoint(Point.xr(2.0, 20.0), 2.0)

        assertThat(f0.lerp( 0.3, f2), `is`(closeTo(ParamPoint(Point.xr(1.3, 13.0), 1.3))))
        assertThat(f0.lerp(-1.0, f2), `is`(closeTo(ParamPoint(Point.xr(0.0, 40.0), 0.0))))
        assertThat(f0.lerp( 2.0, f2), `is`(closeTo(ParamPoint(Point.xr(3.0, 50.0), 3.0))))
        assertThat(f0.lerp( 0.0, f2), `is`(closeTo(ParamPoint(Point.xr(1.0, 10.0), 1.0))))
        assertThat(f0.lerp( 1.0, f2), `is`(closeTo(ParamPoint(Point.xr(2.0, 20.0), 2.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = ParamPoint(Point.xr(1.0, 10.0), 1.0)
        val a = t.toString().parseJson().tryMap { ParamPoint.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(t)))
    }
}