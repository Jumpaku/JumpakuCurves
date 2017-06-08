package org.jumpaku.core.affine


fun Double.divide(t: Double, other: Double): Double = (1-t)*this + t*other

interface Divisible<P> {
    fun divide(t: Double, p: P): P
}