package jumpaku.core.util

import io.vavr.control.Option

infix fun Double.divOption(divisor: Double): Option<Double> = Option.`when`((this/divisor).isFinite(), this/divisor)

fun Double.divOrElse(divisor: Double, default: Double): Double = this.divOption(divisor).getOrElse(default)
