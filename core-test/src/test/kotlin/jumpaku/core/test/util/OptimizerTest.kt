package jumpaku.core.test.util

import jumpaku.core.curve.Interval
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.Optimizer
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

class OptimizerTest {

    @Test
    fun testMinimize() {
        println("Minimize")
        val (x, fx) = Optimizer().minimize(Interval(3.0, 4.0)) { cos(it) }.orThrow()
        x.shouldBeCloseTo(PI)
        fx.shouldBeCloseTo(-1.0)
    }

    @Test
    fun testMaximize() {
        println("Maximize")
        val (x, fx) = Optimizer().maximize(Interval(3.0, 4.0)) { -cos(it) }.orThrow()
        x.shouldBeCloseTo(PI)
        fx.shouldBeCloseTo(1.0)
    }
}