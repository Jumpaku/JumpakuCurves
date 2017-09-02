package jumpaku.core.affine


fun Double.divide(t: Double, other: Double): Double = (1 - t)*this + t*other

fun Double.middle(other: Double): Double = this.divide(0.5, other)

interface Divisible<P> {

    fun divide(t: Double, p: P): P

    fun middle(p: P): P = this.divide(0.5, p)
}