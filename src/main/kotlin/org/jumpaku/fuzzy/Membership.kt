package org.jumpaku.fuzzy

import kotlin.jvm.functions.Function1


interface Membership<M : Membership<M, T>, T> {

    fun membership(t: T): Grade

    fun possibility(u: M): Grade

    fun necessity(u: M): Grade
}