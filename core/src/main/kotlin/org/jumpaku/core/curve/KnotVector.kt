package org.jumpaku.core.curve

import io.vavr.API
import io.vavr.Tuple2
import io.vavr.API.Array
import io.vavr.API.Option
import io.vavr.collection.Stream
import io.vavr.collection.Array
import io.vavr.control.Option
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.divide
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import java.util.Comparator


class KnotVector(val knots: Array<Knot>) : Iterable<Double> {

    constructor(knots: Iterable<Knot>) : this(Array.ofAll(knots))

    constructor(vararg knots: Knot) : this(Array(*knots))

    val value: Array<Double> = knots.flatMap { Stream.fill(it.multiplicity, it::value)}

    val multiplicity: Array<Int> = knots.flatMap { Stream.fill(it.multiplicity, it::multiplicity)}

    init {
        require(value.zip(value.tail()).forAll { (a, b) -> a <= b }) { "not ordered in acceding" }
    }

    operator fun get(i: Int): Double = value[i]

    override fun iterator(): Iterator<Double> = value.iterator()

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): KnotVectorJson = KnotVectorJson(this)

    fun size(): Int = value.size()

    fun lastIndexUnder(t: Double): Int = value.lastIndexWhere { it <= t }

    fun reverse(degree: Int): KnotVector {
        val (begin, end) = domain(degree)
        return KnotVector(knots.map { (v, m) -> Knot(end - v + begin, m) }.reverse())
    }

    fun innerKnotVector(): KnotVector =  removeBack(1).removeFront(1)

    private fun removeBack(n: Int): KnotVector {
        var knotVector = this
        for(i in 1..n){
            knotVector = KnotVector(knotVector.knots.let { when(it.last().multiplicity) {
                1 -> it.init()
                else -> it.update(it.size() - 1, { it.reduce() })
            } })
        }
        return knotVector
    }

    private fun removeFront(n: Int): KnotVector {
        var knotVector = this
        for(i in 1..n){
            knotVector = KnotVector(knotVector.knots.let { when (it.head().multiplicity) {
                1 -> it.tail()
                else -> it.update(0, { it.reduce() })
            } })
        }
        return knotVector
    }

    fun subdivide(degree: Int, t: Double): Array<KnotVector> {
        require(t in domain(degree)) { "t($t) is out of domain(${domain(degree)}" }
        val times = clampInsertionTimes(degree, t, degree + 1)
        val inserted = insertKnot(degree, t, times).knots
        val front = KnotVector(inserted.filter { it.value <= t })
        val back = KnotVector(inserted.filter { it.value >= t })

        return Array(front, back).filter { it.size() - degree - 1 > 0 }
    }

    fun insertKnot(degree: Int, t: Double, maxInsertionTimes: Int = 1): KnotVector {
        require(t in domain(degree)) { "t($t) is out of domain(${domain(degree)})" }
        val times = clampInsertionTimes(degree, t, maxInsertionTimes)
        if(times == 0 ){
            return this
        }
        return indexCloseTo(t)
                .map { multiplyKnot(degree, it, times) }
                .getOrElse {
                    val index = -knots.search(Knot(t), Comparator.comparing(Knot::value)) - 1
                    val tmp = KnotVector(knots.insert(index, Knot(t, times)))
                    when (tmp.indexCloseTo(t).get()) {
                        degree -> tmp.removeFront(times)
                        tmp.size() - degree - 1 -> tmp.removeBack(times)
                        else -> tmp
                    }
                }
    }

    fun multiplyKnot(degree: Int, index: Int, maxInsertionTimes: Int = 1): KnotVector {
        require(value[index] in domain(degree)) { "t(${value[index]}) is out of domain(${domain(degree)})" }
        val times = clampInsertionTimes(degree, value[index], maxInsertionTimes)
        if(times == 0 ){
            return this
        }
        val indexRanges = knots.tail().foldLeft(Array(0..(knots[0].multiplicity - 1)), { acc, knot ->
                    val indexRange = acc.last()
                    acc.append((indexRange.endInclusive + 1)..(indexRange.endInclusive + knot.multiplicity))
                })
        val tmp = knots.zipWith(indexRanges, { knot, indexRange -> when (index) {
            in indexRange -> knot.multiply(times)
            else -> knot
        } }).run(::KnotVector)

        return when(index){
            degree -> tmp.removeFront(times)
            size() - degree - 1 -> tmp.removeBack(times)
            else -> tmp
        }
    }

    fun indexCloseTo(t: Double): Option<Int> {
        val index = value.indexWhere { Precision.equals(it, t, 1.0e-10) }
        return if(index < 0){ Option.none() } else { Option(index) }
    }

    fun clampInsertionTimes(degree: Int, knotValue: Double, maxInsertionTimes: Int): Int {
        val m = indexedKnotCloseTo(knotValue).map { (knot, _) -> knot.multiplicity } .getOrElse(0)
        return maxOf(0, minOf(degree + 1 - m, maxInsertionTimes))
    }

    fun domain(degree: Int): Interval = Interval(value[degree], value.reverse()[degree])

    private fun indexedKnotCloseTo(t: Double): Option<Tuple2<Knot, Int>> = knots.zipWithIndex()
            .find { (iKnot, _) -> Precision.equals(t, iKnot.value, 1.0e-10) }

    companion object {

        fun clamped(degree: Int, vararg values: Double): KnotVector {
            return KnotVector(Array.ofAll(values.mapIndexed { index, value ->
                Knot(value, when (index){
                    0, values.lastIndex -> degree + 1
                    else -> 1
                }) }))
        }

        fun clampedUniform(begin: Double, end: Double, degree: Int, knotSpan: Double): KnotVector {
            return clampedUniform(Interval(begin, end), degree, knotSpan)
        }

        fun clampedUniform(domain: Interval, degree: Int, knotSpan: Double): KnotVector {
            return clampedUniform(domain.begin, domain.end, degree, domain.sample(knotSpan).size() + degree*2)
        }

        fun clampedUniform(degree: Int, knotSize: Int): KnotVector {
            return clampedUniform(0.0, knotSize - 1.0 - 2 * degree, degree, knotSize)
        }

        fun clampedUniform(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            return clampedUniform(domain.begin, domain.end, degree, knotSize)
        }

        fun clampedUniform(begin: Double, end: Double, degree: Int, knotSize: Int): KnotVector {
            val l = knotSize - 2 * degree - 1
            return KnotVector(API.Stream(Knot(begin, degree + 1))
                    .appendAll(Stream.range(1, l).map { Knot(begin.divide(it / l.toDouble(), end), 1) })
                    .append(Knot(end, degree + 1))
                    .toArray())
        }
    }
}

typealias KnotVectorJson = List<KnotJson>

fun KnotVectorJson(knots: List<KnotJson>): KnotVectorJson = knots

fun KnotVectorJson(knotVector: KnotVector): KnotVectorJson = KnotVectorJson(knotVector.knots.map(Knot::json).toJavaList())

fun KnotVectorJson.knotVector(): KnotVector = KnotVector(map(KnotJson::knot))
