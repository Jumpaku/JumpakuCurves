package org.jumpaku.core.curve

import org.jumpaku.core.affine.Vector



interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}