package jumpaku.curves.core.geom


fun Double.lerp(t: Double, other: Double): Double = (1 - t) * this + t * other

fun Double.lerp(vararg terms: Pair<Double, Double>): Double = lerp(terms.toList())

fun Double.lerp(terms: List<Pair<Double, Double>>): Double {
    val c0 = 1 - terms.sumOf { it.first }
    val x0 = this
    return terms.sumOf { (x, c) -> c * x } + (c0 * x0)
}

fun Double.middle(other: Double): Double = this.lerp(0.5, other)


interface Lerpable<P : Lerpable<P>> {

    fun lerp(terms: List<Pair<Double, P>>): P

    fun lerp(vararg terms: Pair<Double, P>): P = lerp(terms.toList())

    fun lerp(t: Double, p: P): P = lerp(t to p)

    fun middle(p: P): P = this.lerp(0.5, p)
}
