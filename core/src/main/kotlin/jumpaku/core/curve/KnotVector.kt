package jumpaku.core.curve

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.geom.divide
import jumpaku.core.json.ToJson
import jumpaku.core.util.*


data class Knot(val value: Double, val multiplicity: Int = 1): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("value" to value, "multiplicity" to multiplicity)

    companion object {

        fun fromJson(json: JsonElement): Result<Knot> = result {
            Knot(json["value"].double, json["multiplicity"].int)
        }
    }
}

class KnotVector private constructor(val degree: Int, val knots: List<Knot>): ToJson {

    val domain: Interval by lazy { extractedKnots.run { Interval(get(degree), get(lastIndex - degree)) } }

    val extractedKnots: List<Double> = knots.flatMap { (v, m) -> List(m) { v } }

    constructor(degree: Int, knots: Iterable<Knot>) : this(degree, knots.toList())

    constructor(degree: Int, vararg knots: Knot) : this(degree, knots.toList())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("degree" to degree, "knots" to jsonArray(knots.map { it.toJson() }))

    fun multiplicityOf(knotValue: Double): Int = search(knotValue).let { if (it < 0) 0 else knots[it].multiplicity }

    fun lastExtractedIndexUnder(value: Double): Int = extractedKnots
            .asVavr()
            .run { slice(degree, size() - degree) }.zipWithNext { a, b -> value in a..b }.indexOfLast { it } + degree

    fun insert(knotValue: Double, times: Int): KnotVector {
        val i = search(knotValue)
        return when {
            times == 0 -> this
            i >= 0 -> multiply(i, times)
            else -> KnotVector(degree, knots.asVavr().insert(-i-1, Knot(knotValue, times)))
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
            .run { if (head().multiplicity == 1) tail() else update(0) { (v, m) -> Knot(v, m - 1) } }
            .run { if (last().multiplicity == 1) init() else update(lastIndex){ (v, m) -> Knot(v, m - 1) } })

    fun subdivide(t: Double): Tuple2<Option<KnotVector>, Option<KnotVector>> {
        val s = multiplicityOf(t)
        val p = degree
        val times = p + 1 - s
        val inserted = insert(t, times)
        val i = inserted.search(t)
        val (b, e) = domain
        val kv = inserted.knots.asVavr()
        val front = if (t == b) kv.take(i + 1).insert(i, Knot(t, s)) else kv.take(i + 1)
        val back = if (t == e) kv.drop(i).insert(1, Knot(t, s)) else kv.drop(i)
        return Tuple2(
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
            v0 < v1 -> -1; v0 > v1 -> 1
            else -> 0
        }
    }

    companion object {

        fun fromJson(json: JsonElement): Result<KnotVector> = result {
            KnotVector(json["degree"].int, json["knots"].array.flatMap { Knot.fromJson(it).value() })
        }

        fun uniform(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            val h = degree
            val l = knotSize - 1 - degree
            return KnotVector(degree,
                    (0 until knotSize).map { Knot(domain.begin.divide((it.toDouble() - h) / (l - h), domain.end)) })
        }

        fun clamped(domain: Interval, degree: Int, knotSize: Int): KnotVector {
            val nSpans = knotSize - 2 * degree - 1
            val (b, e) = domain
            return KnotVector(degree,
                    listOf(Knot(b, degree + 1)) +
                            (1 until nSpans).map { Knot(b.divide(it / nSpans.toDouble(), e)) } +
                            listOf(Knot(e, degree + 1)))
        }
    }
}
