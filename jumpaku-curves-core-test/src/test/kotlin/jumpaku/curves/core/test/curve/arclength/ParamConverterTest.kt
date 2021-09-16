package jumpaku.curves.core.test.curve.arclength

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.LinearFit
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.closeTo
import org.apache.commons.math3.util.FastMath
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class ParamConverterTest {

    val r = LinearFit(
        listOf(
            1.0,
            2.0,
            3.0,
            4.0,
        ),
        listOf(
            2.0,
            4.0,
            8.0,
            16.0
        )
    )

    @Test
    fun testRange() {
        println("Range")
        assertThat(r.range, `is`(closeTo(Interval(2.0, 16.0))))
    }

    @Test
    fun testDomain() {
        println("Domain")
        assertThat(r.domain, `is`(closeTo(Interval(1.0, 4.0))))
    }

    @Test
    fun testInvoke() {
        println("Invoke")
        assertThat(r(1.0), `is`(closeTo(2.0, 1.0)))
        assertThat(r(1.5), `is`(closeTo(3.0, 1.0)))
        assertThat(r(2.0), `is`(closeTo(4.0, 1.0)))
        assertThat(r(2.5), `is`(closeTo(6.0, 1.0)))
        assertThat(r(3.0), `is`(closeTo(8.0, 1.0)))
        assertThat(r(3.5), `is`(closeTo(12.0, 1.0)))
        assertThat(r(4.0), `is`(closeTo(16.0, 1.0)))
    }

    @Test
    fun testFinite() {
        println("Finite")
        val r = LinearFit(
            listOf(
                0.0,
                0.5,
                0.5,
                0.5,
                0.5,
                1.0,
            ),
            listOf(
                0.0,
                0.2,
                0.4,
                0.6,
                0.8,
                1.0
            )
        )
        val u = r.invoke(0.5)
        assertThat(u.isFinite(), `is`(true))

    }
}
