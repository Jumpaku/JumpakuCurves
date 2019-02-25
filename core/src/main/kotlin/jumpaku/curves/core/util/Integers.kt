package jumpaku.curves.core.util

fun Int.isOdd(): Boolean = this.and(1) == 1

fun Int.isEven(): Boolean = !isOdd()