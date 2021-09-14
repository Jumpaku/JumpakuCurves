package jumpaku.curves.core.curve.bspline

import jumpaku.curves.core.curve.Interval
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Represents clamped knot vector.
 * The first `degree` + 1 elements equal to `domain.begin`.
 * The last `degree` + 1 elements equal to `domain.end`.
 * The other elements are greater than `domain.begin`, less than `domain.end`, and arranged in non decreasing order.
 */
class KnotVector internal constructor(val degree: Int, knots: List<Double>) : List<Double> by knots {

    init {
        require(degree > 0) { "degree($degree)" }
    }

    val domain: Interval = Interval(get(degree), get(knots.lastIndex - degree))

    fun differentiate(): KnotVector = KnotVector(degree - 1, subList(1, lastIndex))

    fun restrict(begin: Double, end: Double): KnotVector = subdivide(begin).second.subdivide(end).first
    fun restrict(i: Interval): KnotVector = restrict(i.begin, i.end)

    override fun toString(): String = "KnotVector(degree=$degree, knots=${this.toList()})"

    fun searchIndexToInsert(knotValue: Double): Int =
        binarySearch(knotValue, degree, size - degree).let { max(it, -it - 2) }

    fun insertKnot(knotValue: Double, times: Int): KnotVector {
        if (knotValue == domain.end || knotValue == domain.begin) return this
        val index = searchIndexToInsert(knotValue)
        return KnotVector(degree, take(index + 1) + List(times) { knotValue } + drop(index + 1))
    }

    fun subdivide(t: Double): Pair<KnotVector, KnotVector> {
        if (t == domain.end) return Pair(this, KnotVector(degree, List(degree * 2 + 2) { t }))
        if (t == domain.begin) return Pair(KnotVector(degree, List(degree * 2 + 2) { t }), this)
        val index = searchIndexToInsert(t)
        val k0 = KnotVector(degree, take(index + 1) + List(degree + 1) { t })
        val k1 = KnotVector(degree, List(degree + 1) { t } + drop(index + 1))
        return Pair(k0.shrinkMultiplicity(), k1.shrinkMultiplicity())
    }

    private fun shrinkMultiplicity(): KnotVector {
        val cleaned = this.let {
            it.drop(max(0, it.lastIndexOf(it.first()) - degree))
        }.let {
            it.take(min(it.size, it.indexOf(it.last()) + degree + 1))
        }
        return KnotVector(degree, cleaned)
    }

    fun reverse(): KnotVector =
        KnotVector(degree, map { domain.run { (end - it + begin).coerceIn(domain) } }.reversed())


    companion object {

        fun clamped(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            require(degree > 0) { "degree($degree)" }
            require(domain.span > 0.0 || knotSize == degree * 2 + 2) { "domain($domain)" }
            require(knotSize >= degree * 2 + 2) { "knotSize($knotSize)" }

            val knots = domain.run {
                List(degree) { begin } + sample(knotSize - 2 * degree) + List(degree) { end }
            }

            return KnotVector(degree, knots)
        }

        fun clamped(domain: Interval, degree: Int, knotSpan: Double): KnotVector {
            require(degree > 0) { "degree($degree)" }
            require(domain.span > 0.0) { "domain($domain)" }
            require(knotSpan > 0.0) { "knotSpan($knotSpan)" }
            val knotSize = ceil(domain.span / knotSpan).toInt() + 1 + 2 * degree
            return clamped(domain, degree, knotSize)
        }

        fun of(degree: Int, sortedKnots: List<Double>): KnotVector {
            require(sortedKnots.zipWithNext { a, b -> a <= b }.all { it }) { "sortedKnots($sortedKnots) must be sorted" }
            return KnotVector(degree, sortedKnots)
        }
    }
}
