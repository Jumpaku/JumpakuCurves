package jumpaku.core.util


infix fun Double.tryDiv(divisor: Double): Result<Double> = result {
    if ((this/divisor).isFinite()) this / divisor
    else throw ArithmeticException("divide by zero")
}

fun Double.divOrDefault(divisor: Double, default: () -> Double): Double = tryDiv(divisor).value().orDefault(default)
