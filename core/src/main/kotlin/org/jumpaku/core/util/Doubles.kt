package org.jumpaku.core.util

fun Double.nonZero(): Boolean = (1/this).isFinite()
