package jumpaku.core.curve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import jumpaku.core.affine.divide
import jumpaku.core.json.ToJson


class KnotVector(val degree: Int, val knots: Array<Double>) : Iterable<Double>, ToJson {

    constructor(degree: Int, knots: Iterable<Double>) : this(degree, Array.ofAll(knots))

    constructor(degree: Int, vararg knots: Double) : this(degree, Array(*knots.toTypedArray()))

    val domain: Interval = Interval(knots[degree], knots[knots.size() - degree - 1])

    operator fun get(i: Int): Double = knots[i]

    override fun iterator(): Iterator<Double> = knots.iterator()

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("degree" to degree, "knots" to jsonArray(knots.map { it.toJson() }))

    fun size(): Int = knots.size()

    fun lastIndexUnder(t: Double): Int {
        val inner = knots.slice(degree, size() - degree)
        return inner.zipWith(inner.tail(), ::Interval).lastIndexWhere { t in it } + degree
    }

    fun reverse(): KnotVector {
        val (begin, end) = domain
        return KnotVector(degree, knots.map { end - it + begin }.reverse())
    }

    fun derivativeKnotVector(): KnotVector = KnotVector(degree - 1, knots.slice(1, size() - 1))

    fun subdivide(t: Double): Tuple2<KnotVector, KnotVector> {
        require(t in domain) { "t($t) is out of domain($domain" }
        val k = lastIndexUnder(t)
        val inserted = insertKnot(t, degree + 1)
        val front = KnotVector(degree, inserted.take(k + degree + 2))
        val back = KnotVector(degree, inserted.drop(k + 1))

        return Tuple(front, back)
    }

    fun insertKnot(t: Double, maxMultiplicity: Int = 1): KnotVector {
        require(t in domain) { "t($t) is out of domain($domain)" }

        var inserted = this
        for (i in 1..maxMultiplicity){
            val k = inserted.lastIndexUnder(t)
            inserted = KnotVector(degree, inserted.knots.insert(k + 1, t))
        }

        return inserted
    }

    companion object {

        fun ofKnots(degree: Int, vararg knots: Knot): KnotVector = ofKnots(degree, Stream(*knots))

        fun ofKnots(degree: Int, knots: Iterable<Knot>): KnotVector {
            return KnotVector(degree,
                    Stream.ofAll(knots).sortBy { it.value }.flatMap { Stream.fill(it.multiplicity, { it.value }) })
        }

        fun clampedUniform(begin: Double, end: Double, degree: Int, knotSpan: Double): KnotVector {
            return clampedUniform(Interval(begin, end), degree, knotSpan)
        }

        fun clampedUniform(domain: Interval, degree: Int, knotSpan: Double): KnotVector {
            return clampedUniform(domain.begin, domain.end, degree, domain.sample(knotSpan).size() + degree * 2)
        }

        fun clampedUniform(degree: Int, knotSize: Int): KnotVector {
            return clampedUniform(0.0, knotSize - 1.0 - 2 * degree, degree, knotSize)
        }

        fun clampedUniform(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            return clampedUniform(domain.begin, domain.end, degree, knotSize)
        }

        fun clampedUniform(begin: Double, end: Double, degree: Int, knotSize: Int): KnotVector {
            val nSpans = knotSize - 2 * degree - 1
            return KnotVector(degree,
                    Stream.fill(degree + 1, { begin })
                            .appendAll(Stream.range(1, nSpans).map { begin.divide(it / nSpans.toDouble(), end) })
                            .appendAll(Stream.fill(degree + 1, { end })))
        }
    }
}

data class Knot(val value: Double, val multiplicity: Int = 1) {

    init {
        require(multiplicity > 0) { "multiplicity($multiplicity) < 0" }
    }
}

val JsonElement.knotVector: KnotVector get() = KnotVector(this["degree"].int, this["knots"].array.map { it.double })
