package org.jumpaku.core.fuzzy


interface Membership<M : Membership<M, T>, T> {

    fun membership(p: T): Grade

    fun isPossible(u: M): Grade

    fun isNecessary(u: M): Grade
}