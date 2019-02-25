package jumpaku.curves.core.util

import org.apache.commons.math3.util.MathArrays


infix fun Double.tryDiv(divisor: Double): Result<Double> = result {
    if ((this/divisor).isFinite()) this / divisor
    else throw ArithmeticException("divide by zero")
}

fun Double.divOrDefault(divisor: Double, default: () -> Double): Double = tryDiv(divisor).value().orDefault(default)

fun sum(doubles: DoubleArray): Double = MathArrays.linearCombination(doubles, DoubleArray(doubles.size) { 1.0 })
fun sum(doubles: List<Double>): Double = sum(doubles.toDoubleArray())