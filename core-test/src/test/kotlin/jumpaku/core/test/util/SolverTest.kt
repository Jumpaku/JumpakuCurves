package jumpaku.core.test.util

import jumpaku.core.curve.Interval
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.Solver
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SolverTest {

    @Test
    fun testSolve() {
        println("Solve")
        val x = Solver().solve(Interval(3.0, 4.0), 3.5) { sin(it) }.orThrow()
        x.shouldBeCloseTo(PI)
    }
}