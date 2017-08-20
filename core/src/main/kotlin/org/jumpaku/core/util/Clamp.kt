package org.jumpaku.core.util

fun <T : Comparable<T>> clamp(x: T, min: T, max: T): T {
    require(min <= max) { "min($min) > max($max)" }
    return minOf(max, maxOf(min, x))
}


