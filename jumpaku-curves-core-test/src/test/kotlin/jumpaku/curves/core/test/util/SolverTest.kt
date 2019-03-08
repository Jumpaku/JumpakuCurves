package jumpaku.curves.core.test.util

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.util.Solver
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SolverTest {

    @Test
    fun testSolve() {
        println("Solve")
        val x = Solver().solve(Interval(3.0, 4.0), 3.5) { sin(it) }.orThrow()
        assertThat(x, `is`(closeTo(PI)))
    }
}