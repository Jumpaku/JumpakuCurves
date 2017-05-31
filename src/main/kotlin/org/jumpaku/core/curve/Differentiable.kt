package org.jumpaku.curve

import org.jumpaku.affine.Vector



interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}