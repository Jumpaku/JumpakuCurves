package jumpaku.core.curve

import jumpaku.core.geom.Vector


interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}