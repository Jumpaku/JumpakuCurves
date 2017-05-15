package org.jumpaku.curve

import org.jumpaku.affine.Vector


/**
 * Created by jumpaku on 2017/05/13.
 */
interface Differentiable {

    val derivative: Derivative

    fun differentiate(t: Double): Vector = derivative(t)
}