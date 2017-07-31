package org.jumpaku.core.util

fun <T: Comparable<T>> clamp(x: T, min: T, max: T): T = minOf(max, maxOf(min, x))

