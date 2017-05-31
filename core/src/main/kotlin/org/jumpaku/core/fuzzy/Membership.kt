package org.jumpaku.core.fuzzy


interface Membership<M : Membership<M, T>, T> {

    fun membership(p: T): Grade

    fun possibility(u: M): Grade

    fun necessity(u: M): Grade
}