package jumpaku.core.curve.arclength

import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.geom.Point
import io.vavr.collection.Array

fun Curve.approximate(tolerance: Double): Array<Double> {
    return Array.empty()
}

class ReparametrizedCurve(val originalCurve: Curve, val reparametrizer: Reparametrizer): Curve {

    override val reparameterized: ArcLengthReparameterized
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)"}
        return originalCurve(reparametrizer.toOriginal(t))
    }

    override val domain: Interval = reparametrizer.range

    fun computeArcLength(nSamples: Int): Double = domain.sample(nSamples).zipWithNext { s0, s2 ->
        evaluate(s0).dist(evaluate(s2))
    }.sum()
}