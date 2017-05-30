package org.jumpaku.samples

import io.vavr.API


fun main(vararg args: String) {

    val a = API.Array(1, 2, 3)
    val b = a.prepend(4)

    println("a = $a")
    println("b = $b")
}