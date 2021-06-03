package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector

interface Derivative {

    val domain: Interval

    operator fun invoke(t: Double): Vector {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return evaluate(t)
    }

    fun evaluate(t: Double): Vector

    fun evaluateAll(n: Int): List<Vector> = evaluateAll(domain.sample(n))

    fun evaluateAll(delta: Double): List<Vector> = evaluateAll(domain.sample(delta))

    fun evaluateAll(sortedParams: List<Double>): List<Vector> = sortedParams.map { evaluate(it) }
}