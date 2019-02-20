package jumpaku.core.geom

import org.apache.commons.math3.util.MathArrays


fun Double.lerp(t: Double, other: Double): Double = lerp(t to other)

fun Double.lerp(vararg terms: Pair<Double, Double>): Double {
    val (cs, ds) = terms.unzip().let { (c, d) -> c.toDoubleArray() to d.toDoubleArray() }
    val c0 = 1 - MathArrays.linearCombination(cs, DoubleArray(cs.size) { 1.0 })
    return MathArrays.linearCombination(ds + this, cs + c0)
}

fun Double.middle(other: Double): Double = this.lerp(0.5, other)

interface Divisible<P: Divisible<P>> {

    fun lerp(vararg terms: Pair<Double, P>): P

    fun lerp(t: Double, p: P): P = lerp(t to p)

    fun middle(p: P): P = this.lerp(0.5, p)
}
