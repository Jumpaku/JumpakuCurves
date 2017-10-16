package jumpaku.core.util

import jumpaku.core.curve.Interval

fun <T : Comparable<T>> clamp(x: T, min: T, max: T): T {
    require(min <= max) { "min($min) > max($max)" }
    return minOf(max, maxOf(min, x))
}

fun clamp(x: Double, interval: Interval): Double = clamp(x, interval.begin, interval.end)
