package org.jumpaku.fuzzy

import kotlin.jvm.functions.Function1


interface Membership<M : Membership<M, T>, T> : Function1<T, Grade> {

    override fun invoke(t: T) = membership(t)

    fun membership(t: T): Grade

    fun possibility(u: M): Grade

    fun necessity(u: M): Grade
}