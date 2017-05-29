package org.jumpaku.affine


interface Divisible<P> {
    fun divide(t: Double, p: P): P
}