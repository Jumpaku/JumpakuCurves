package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Vector


interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}