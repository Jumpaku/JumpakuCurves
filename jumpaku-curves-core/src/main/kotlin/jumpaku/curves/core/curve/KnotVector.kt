package jumpaku.curves.core.curve

import jumpaku.commons.control.Option
import jumpaku.commons.control.optionWhen
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.util.asVavr
import jumpaku.curves.core.util.lastIndex
import kotlin.math.ceil


data class Knot(val value: Double, val multiplicity: Int = 1) {

    init {
        require(value.isFinite()) { "value($value)" }
        require(multiplicity > 0) { "must be multiplicity($multiplicity) > 0" }
    }


}

class KnotVector(val degree: Int, knots: Iterable<Knot>) {

    constructor(degree: Int, vararg knots: Knot) : this(degree, knots.toList())

    init {
        require(degree >= 0) { "degree($degree)" }
    }

    val knots: List<Knot> = knots.toList()

    val extractedKnots: List<Double> = knots.flatMap { (v, m) -> List(m) { v } }

    val domain: Interval = extractedKnots.run { Interval(get(degree), get(lastIndex - degree)) }

    override fun toString(): String = "KnotVector(degree=$degree, knots=$knots)"

    fun multiplicityOf(knotValue: Double): Int =
            search(knotValue).let { if (it < 0) 0 else knots[it].multiplicity }

    fun searchLastExtractedLessThanOrEqualTo(t: Double): Int {
        require(domain.begin <= t && t < domain.end)
        val us = extractedKnots
        var a = degree
        var b = us.lastIndex - degree
        while (true) {
            val c = (a + b).ushr(1)
            when {
                a == b -> return c
                t < us[c] -> b = c - 1
                us[c + 1] <= t -> a = c + 1
                else/* us[c] <= t && t < us[c + 1] */ -> return c
            }
        }
    }

    fun insert(knotValue: Double, times: Int): KnotVector {
        val i = search(knotValue)
        return when {
            times == 0 -> this
            i >= 0 -> multiply(i, times)
            else -> KnotVector(degree, knots.asVavr().insert(-i - 1, Knot(knotValue, times)))
        }
    }

    fun multiply(knotIndex: Int, times: Int): KnotVector {
        val knot = knots[knotIndex]
        return KnotVector(degree, knots.asVavr().update(knotIndex, knot.copy(multiplicity = knot.multiplicity + times)))
    }

    fun remove(knotIndex: Int, times: Int): KnotVector {
        val ks = knots.asVavr()
        val (u, s) = ks[knotIndex]
        return KnotVector(degree,
                if (s <= times) ks.removeAt(knotIndex) else ks.update(knotIndex, Knot(u, s - times)))
    }

    fun derivativeKnotVector(): KnotVector = KnotVector(degree - 1, knots
            .asVavr()
            .run {
                if (head().multiplicity == 1) tail()
                else update(0) { (v, m) -> Knot(v, m - 1) }
            }
            .run {
                if (last().multiplicity == 1) init()
                else update(lastIndex) { (v, m) -> Knot(v, m - 1) }
            })

    fun subdivide(t: Double): Pair<Option<KnotVector>, Option<KnotVector>> {
        val s = multiplicityOf(t)
        val p = degree
        val times = p + 1 - s
        val inserted = insert(t, times)
        val i = inserted.search(t)
        val (b, e) = domain
        val kv = inserted.knots.asVavr()
        val front = if (t == b) kv.take(i + 1).insert(i, Knot(t, s)) else kv.take(i + 1)
        val back = if (t == e) kv.drop(i).insert(1, Knot(t, s)) else kv.drop(i)
        return Pair(
                optionWhen(t > b) { KnotVector(p, front) },
                optionWhen(t < e) { KnotVector(p, back) })
    }

    fun reverse(): KnotVector =
            domain.let { (b, e) -> KnotVector(degree, knots.map { it.copy(value = e - it.value + b) }.reversed()) }

    fun clamp(): KnotVector {
        val (b, e) = domain
        val bh = degree + 1 - multiplicityOf(b)
        val eh = degree + 1 - multiplicityOf(e)
        return KnotVector(degree, insert(b, bh).insert(e, eh).knots.filter { it.value in domain })
    }

    fun search(knotValue: Double): Int = knots.asVavr().search(Knot(knotValue)) { (v0), (v1) ->
        when {
            v0 < v1 -> -1
            v0 > v1 -> 1
            else -> 0
        }
    }

    companion object {


        fun uniform(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            require(degree >= 0) { "degree($degree)" }
            require(knotSize > 1) { "knotSize($knotSize)" }
            val h = degree
            val l = knotSize - 1 - degree
            return KnotVector(degree,
                    (0 until knotSize).map { Knot(domain.begin.lerp((it.toDouble() - h) / (l - h), domain.end)) })
        }

        fun uniform(domain: Interval, degree: Int, knotSpan: Double): KnotVector {
            require(degree >= 0) { "degree($degree)" }
            require(knotSpan > 0.0) { "knotSpan($knotSpan)" }
            val knotSize = ceil(domain.span / knotSpan).toInt() + 1 + 2 * degree
            return uniform(domain, degree, knotSize)
        }

        fun clamped(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            require(degree >= 0) { "degree($degree)" }
            require(knotSize > 1) { "knotSize($knotSize)" }
            val nSpans = knotSize - 2 * degree - 1
            val (b, e) = domain
            val middle = (1 until nSpans).map { Knot(b.lerp(it / nSpans.toDouble(), e)) }
            return KnotVector(degree,
                    listOf(Knot(b, degree + 1)) + middle + listOf(Knot(e, degree + 1)))
        }

        fun clamped(domain: Interval, degree: Int, knotSpan: Double): KnotVector {
            require(degree >= 0) { "degree($degree)" }
            require(knotSpan > 0.0) { "knotSpan($knotSpan)" }
            val knotSize = ceil(domain.span / knotSpan).toInt() + 1 + 2 * degree
            return clamped(domain, degree, knotSize)
        }
    }
}
