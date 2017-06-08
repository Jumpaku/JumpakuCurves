package org.jumpaku.core.curve

import org.jumpaku.core.affine.Vector

interface Derivative {

    val domain: Interval

    operator fun invoke(t: Double): Vector {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain($domain)")
        }

        return evaluate(t)
    }

    fun evaluate(t: Double): Vector
}