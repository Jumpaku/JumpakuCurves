package jumpaku.curves.core.test.util

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.test.shouldBeCloseTo
import jumpaku.curves.core.util.Optimizer
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

class OptimizerTest {

    @Test
    fun testMinimize() {
        println("Minimize")
        val (x, fx) = Optimizer().minimize(Interval(3.0, 4.0)) { cos(it) }.orThrow()
        assertThat(x, `is`(closeTo(PI)))
        assertThat(fx, `is`(closeTo(-1.0)))
    }

    @Test
    fun testMaximize() {
        println("Maximize")
        val (x, fx) = Optimizer().maximize(Interval(3.0, 4.0)) { -cos(it) }.orThrow()
        assertThat(x, `is`(closeTo(PI)))
        assertThat(fx, `is`(closeTo(1.0)))
    }
}