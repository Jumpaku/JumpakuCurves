package jumpaku.core.util


infix fun Double.divOption(divisor: Double): Option<Double> = optionWhen((this/divisor).isFinite()) { this/divisor }

fun Double.divOrElse(divisor: Double, default: Double): Double = this.divOption(divisor).orDefault(default)
