package org.jumpaku.core.curve

import io.vavr.Tuple2


interface Subdividible<C> {
    fun subdivide(t: Double): Tuple2<C, C>
}