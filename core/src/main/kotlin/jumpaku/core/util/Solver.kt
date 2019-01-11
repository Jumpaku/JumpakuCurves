package jumpaku.core.util

import jumpaku.core.curve.Interval
import jumpaku.core.geom.divide
import org.apache.commons.math3.analysis.solvers.BrentSolver


class Solver {

    fun solve(interval: Interval, initial: Double = interval.sample(3)[1], function: (Double) -> Double): Result<Double> {
        require(initial in interval) { "initial value($initial) out of interval($interval)" }
        fun f(x: Double): Double = function(interval.run { begin.divide(x, end).coerceIn(interval) })
        val x0 = interval.run { ((initial - begin) / span).coerceIn(Interval.ZERO_ONE) }
        return result {
            val x = BrentSolver(1e-15, 1e-10).solve(50, { f(it) }, 0.0, 1.0, x0)
            interval.run { begin.divide(x, end).coerceIn(interval) }
        }
    }
}

