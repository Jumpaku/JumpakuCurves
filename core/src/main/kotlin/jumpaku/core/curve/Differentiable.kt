package jumpaku.core.curve

import jumpaku.core.affine.Vector



interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}