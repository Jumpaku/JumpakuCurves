package org.jumpaku.core.affine


interface Divisible<P> {
    fun divide(t: Double, p: P): P
}