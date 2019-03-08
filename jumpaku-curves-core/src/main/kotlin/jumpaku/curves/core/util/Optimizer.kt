package jumpaku.curves.core.util

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.lerp
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.univariate.BrentOptimizer
import org.apache.commons.math3.optim.univariate.SearchInterval
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction

class Optimizer {

    fun minimize(interval: Interval, objectiveFunction: (Double) -> Double): Result<Pair<Double, Double>> = result {
        fun f(x: Double): Double = objectiveFunction(interval.run { begin.lerp(x, end).coerceIn(interval) })
        val optimum = BrentOptimizer(1e-15, 1e-10).optimize(
                UnivariateObjectiveFunction { f(it) },
                MaxEval(50),
                GoalType.MINIMIZE,
                SearchInterval(0.0, 1.0))
        optimum.run { interval.run { begin.lerp(point, end).coerceIn(interval) } to value }
    }

    fun maximize(interval: Interval, f: (Double) -> Double): Result<Pair<Double, Double>> =
            minimize(interval) { -f(it) }.tryMap { (x, fx) -> x to -fx }
}