package org.jumpaku.affine

/**
 * Created by jumpaku on 2017/05/09.
 */
interface Divisible<P> {
    fun divide(t: Double, p: P): P
}