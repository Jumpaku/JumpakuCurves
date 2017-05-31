package org.jumpaku.curve

import org.jumpaku.affine.Vector

interface Derivative {

    val domain: Interval

    operator fun invoke(t: Double): Vector {
        if (t !in domain) {
            throw IllegalArgumentException("t=$t is out of $domain")
        }

        return evaluate(t)
    }

    fun evaluate(t: Double): Vector
}