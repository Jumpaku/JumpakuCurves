package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.*
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.arclength.repeatBisection
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.divOption
import jumpaku.core.util.lastIndex
import org.apache.commons.math3.util.Precision


class BSpline(val controlPoints: Array<Point>, val knotVector: KnotVector)
    : FuzzyCurve, Differentiable, Transformable, Subdividible<BSpline>, ToJson {

    val degree: Int = knotVector.degree

    override val domain: Interval = knotVector.domain

    override val derivative: BSplineDerivative get() {
        val us = knotVector.extract()
        val cvs = controlPoints
                .zipWith(controlPoints.tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .zipWithIndex({ v, i ->
                    v* basisHelper(degree.toDouble(), 0.0, us[degree + i + 1], us[i + 1])
                })

        return BSplineDerivative(cvs, knotVector.derivativeKnotVector())
    }

    init {
        val us = knotVector.extract()
        val p = knotVector.degree
        val n = controlPoints.size()
        val m = us.size()
        require(n >= p + 1) { "controlPoints.size()($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size()($m) - degree($p) - 1 != controlPoints.size()($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
    }

    constructor(controlPoints: Iterable<Point>, knotVector: KnotVector) : this(Array.ofAll(controlPoints), knotVector)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

    override fun reparametrizeArcLength(): ArcLengthReparametrized {
        val ts = repeatBisection(this, this.domain, { bSpline, subDomain ->
            val cp = bSpline.restrict(subDomain).controlPoints
            val polylineLength = Polyline(cp).reparametrizeArcLength().arcLength()
            val beginEndLength = cp.head().dist(cp.last())
            !Precision.equals(polylineLength, beginEndLength, 1.0 / 256)
        }).fold(Stream(domain.begin), { acc, subDomain -> acc.append(subDomain.end) })

        return ArcLengthReparametrized(this, ts.toArray())
    }

    override fun toCrisp(): BSpline = BSpline(controlPoints.map { it.toCrisp() }, knotVector)

    override fun evaluate(t: Double): Point = evaluate(controlPoints, knotVector, t)

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    override fun transform(a: Affine): BSpline = BSpline(controlPoints.map(a), knotVector)

    fun restrict(begin: Double, end: Double): BSpline {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().subdivide(end)._1()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(controlPoints.reverse(), knotVector.reverse())

    /**
     * Multiplies degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): BSpline = BSpline(clampedControlPoints(controlPoints, knotVector), knotVector.clamp())

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline = BSpline(clampedControlPoints(controlPoints, knotVector), knotVector.clamp())

    fun toBeziers(): Array<Bezier> = segmentedControlPoints(controlPoints, knotVector)
            .run { slice(degree, size() - degree) }
            .sliding(degree + 1, degree + 1)
            .map { Bezier(it) }
            .toArray()

    override fun subdivide(t: Double): Tuple2<BSpline, BSpline> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val (cp0, cp1) = subdividedControlPoints(t, controlPoints, knotVector)
        val (kv0, kv1) = knotVector.subdivide(t)
        return Tuple2(BSpline(cp0, kv0), BSpline(cp1, kv1))
    }

    fun insertKnot(t: Double, times: Int = 1): BSpline {
        require(t in domain) { "t($t) is out of domain($domain)." }
        val s = knotVector.multiplicityOf(t)
        val p = knotVector.degree
        val h = times.coerceIn(0..(p + 1 - s))
        return BSpline(insertedControlPoints(controlPoints, knotVector, t, h), knotVector.insert(t, h))
    }

    fun removeKnot(knotIndex: Int, times: Int = 1): BSpline {
        val s = knotVector.knots[knotIndex].multiplicity
        val h = times.coerceIn(0..s)
        return BSpline(removedControlPoints(controlPoints, knotVector, knotIndex, h), knotVector.remove(knotIndex, h))
    }

    companion object {

        fun fromJson(json: JsonElement): Option<BSpline> = Try.ofSupplier {
            BSpline(json["controlPoints"].array.flatMap { Point.fromJson(it) }, KnotVector.fromJson(json["knotVector"]).get())
        }.toOption()

        fun <D : Divisible<D>> evaluate(controlPoints: Array<D>, knotVector: KnotVector, t: Double): D {
            val l = knotVector.lastIndexUnder(t)
            val us = knotVector.extract()
            if(l == us.size() - 1){
                return controlPoints.last()
            }

            val result = controlPoints.toMutableList()
            val d = knotVector.degree

            for (k in 1..d) {
                for (i in l downTo (l - d + k)) {
                    val aki = basisHelper(t, us[i], us[i + d + 1 - k], us[i])
                    result[i] = result[i - 1].divide(aki, result[i])
                }
            }

            return result[l]
        }

        fun <D : Divisible<D>> insertedControlPoints(controlPoints: Array<D>, knotVector: KnotVector, knotValue: Double, times: Int): Array<D> {
            val us = knotVector.extract()
            val p = us.size() - controlPoints.size() - 1
            val s = knotVector.multiplicityOf(knotValue)
            val k = knotVector.lastIndexUnder(knotValue)
            val h = times.coerceIn(0..(p + 1))
            var cp = controlPoints.toJavaList()

            for (r in 1..h) {
                val front = cp.take(k - p + r)
                val back = cp.drop(k - s)
                for (i in (k - s) downTo (k - p + r)) {
                    val a = basisHelper(knotValue, us[i], us[i + p - r + 1], us[i])
                    cp[i] = cp[i - 1].divide(a, cp[i])
                }
                cp = front + cp.slice((k - p + r)..(k - s)) + back
            }
            return Array.ofAll(cp)
        }

        fun <D : Divisible<D>> removedControlPoints(controlPoints: Array<D>, knotVector: KnotVector, knotIndex: Int, times: Int): Array<D> {
            val us = knotVector.extract()
            val n = controlPoints.size()
            val p = us.size() - n - 1
            val u = knotVector.knots[knotIndex].value
            val s = knotVector.multiplicityOf(u)
            val k = knotVector.lastIndexUnder(u)
            val h = times.coerceIn(0..s)

            if (h == 0) return controlPoints
            if (s > p) return removedControlPoints(
                    controlPoints.removeAt(k - p), knotVector.remove(knotIndex, times - 1), knotIndex, times - 1)

            val cp = controlPoints.toMutableList()
            for (t in 1..h) {
                var i = k - p - t + 1
                var j = k - s + t - 1
                while (j - i >= t) {
                    val ai = basisHelper(u, us[i], us[i + p + t], us[i])
                    cp[i] = cp[i].divide((ai - 1)/ai, cp[i - 1])
                    ++i
                    val aj = basisHelper(u, us[j - t + 1], us[j + p + 1], us[j - t + 1])
                    cp[j] = cp[j].divide(aj/(aj - 1), cp[j + 1])
                    --j
                }
            }
            val f = k - p - h + 1
            val a = (2*k - p - s - h)/2
            val l = k - s + h - 1
            val b = (2*k - p - s + h + 2)/2

            return Array.ofAll(cp.take(f) + cp.slice(f..a) + cp.slice(b..l) + cp.drop(l + 1))
        }

        fun <D : Divisible<D>> subdividedControlPoints(t: Double, controlPoints: Array<D>, knotVector: KnotVector): Tuple2<Array<D>, Array<D>> {
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(t)
            val k = knotVector.lastIndexUnder(t)
            val cp = insertedControlPoints(controlPoints, knotVector, t, p + 1 - s)
            return Tuple2(cp.take(k - p), cp.drop(k - p))
        }

        fun <D : Divisible<D>> segmentedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
            val p = knotVector.degree
            var tmpControlPoints = controlPoints
            var tmpKnots = knotVector
            val domain = knotVector.domain
            knotVector.knots.filter { it.value in domain }.forEach { (u, s) ->
                val times = p + 1 - s
                tmpControlPoints = insertedControlPoints(tmpControlPoints, tmpKnots, u, times)
                tmpKnots = tmpKnots.insert(u, times)
            }
            return tmpControlPoints
        }

        fun <D : Divisible<D>> clampedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
            val p = knotVector.degree
            var tmpControlPoints = controlPoints
            var tmpKnots = knotVector
            val (b, e) = knotVector.domain
            knotVector.knots.filter { it.value == b || it.value == e }.forEach { (u, s) ->
                val times = p + 1 - s
                tmpControlPoints = insertedControlPoints(tmpControlPoints, tmpKnots, u, times)
                tmpKnots = tmpKnots.insert(u, times)
            }
            return tmpControlPoints.run { slice(p, size() - p) }
        }

        fun <D : Divisible<D>> closedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
            val cp = clampedControlPoints(controlPoints, knotVector)
            val closeAt = cp.run { head().middle(last()) }
            return cp.update(0, closeAt).update(cp.lastIndex, closeAt)
        }

        fun basis(t: Double, i: Int, knotVector: KnotVector): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            val us = knotVector.extract()
            val p = knotVector.degree
            if (Precision.equals(t, us[p], 1.0e-10)) return if (i == 0) 1.0 else 0.0
            if (Precision.equals(t, us[us.size() - p - 1], 1.0e-10)) return if (i == us.size() - p - 2) 1.0 else 0.0

            val l = knotVector.lastIndexUnder(t)
            val ns = (0..p).map { index -> if ( index == l - i ) 1.0 else 0.0}.toMutableList()

            for (j in 1..(ns.lastIndex)) {
                for (k in 0..(ns.lastIndex - j)) {
                    val left = basisHelper(t, us[k + i], us[k + i + j], us[k + i])
                    val right = basisHelper(us[k + 1 + i + j], t, us[k + 1 + i + j], us[k + 1 + i])
                    ns[k] = left * ns[k] + right * ns[k + 1]
                }
            }

            return ns[0]
        }

        fun basisHelper(a: Double, b: Double, c: Double, d: Double): Double = (a - b).divOption (c - d).getOrElse(0.0)
    }
}

class KnotVector(val degree: Int, val knots: Array<Knot>): ToJson {

    data class Knot(val value: Double, val multiplicity: Int = 1): ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject("value" to value, "multiplicity" to multiplicity)

        companion object {

            fun fromJson(json: JsonElement): Option<Knot> = Try.ofSupplier {
                Knot(json["value"].double, json["multiplicity"].int)
            }.toOption()
        }
    }

    val domain: Interval by lazy { extract().run { Interval(get(degree), get(lastIndex - degree)) } }

    constructor(degree: Int, knots: Iterable<Knot>) : this(degree, Array.ofAll(knots))

    constructor(degree: Int, vararg knots: Knot) : this(degree, Array.of(*knots))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("degree" to degree, "knots" to jsonArray(knots.map { it.toJson() }))

    fun extract(): Array<Double> = knots.flatMap { (v, m) -> Stream.fill(m) { v } }

    fun multiplicityOf(knotValue: Double): Int = search(knotValue).let { if (it < 0) 0 else knots[it].multiplicity }

    fun lastIndexUnder(value: Double): Int = extract().zipWithNext { a, b -> value in a..b }.indexOfLast { it }

    fun insert(knotValue: Double, times: Int): KnotVector {
        val i = search(knotValue)
        return if (i >= 0) multiply(i, times)
        else KnotVector(degree, knots.insert(-i-1, Knot(knotValue, times)))
    }

    fun multiply(knotIndex: Int, times: Int): KnotVector {
        val knot = knots[knotIndex]
        return KnotVector(degree, knots.update(knotIndex, knot.copy(multiplicity = knot.multiplicity + times)))
    }

    fun remove(knotIndex: Int, times: Int): KnotVector {
        val (u, s) = knots[knotIndex]
        return KnotVector(degree,
                if (s <= times) knots.removeAt(knotIndex) else knots.update(knotIndex, Knot(u, s - times)))
    }

    fun derivativeKnotVector(): KnotVector = KnotVector(degree - 1, knots
            .run { if (head().multiplicity == 1) tail() else update(0) { (v, m) -> Knot(v, m - 1) } }
            .run { if (last().multiplicity == 1) init() else update(lastIndex){ (v, m) -> Knot(v, m - 1) } })

    fun subdivide(t: Double): Tuple2<KnotVector, KnotVector> {
        val s = multiplicityOf(t)
        val inserted = insert(t, degree + 1 - s)
        val i = inserted.search(t)
        return Tuple2(KnotVector(degree, inserted.knots.take(i + 1)), KnotVector(degree, inserted.knots.drop(i)))
    }

    fun reverse(): KnotVector =
            domain.let { (b, e) -> KnotVector(degree, knots.map { it.copy(value = e - it.value + b) }.reverse()) }

    fun clamp(): KnotVector {
        val (b, e) = domain
        val bh = degree + 1 - multiplicityOf(b)
        val eh = degree + 1 - multiplicityOf(e)
        return KnotVector(degree, insert(b, bh).insert(e, eh).knots.filter { it.value in domain })
    }

    private fun search(knotValue: Double): Int =
            knots.search(Knot(knotValue)) { (v0), (v1) -> when { v0 < v1 -> -1; v0 > v1 -> 1; else -> 0 } }

    companion object {

        fun fromJson(json: JsonElement): Option<KnotVector> = Try.ofSupplier {
            KnotVector(json["degree"].int, Array.ofAll(json["knots"].array.flatMap { Knot.fromJson(it) }))
        }.toOption()

        fun uniform(domain: Interval, degree: Int, knotSize: Int): KnotVector = KnotVector(degree,
                (0 until knotSize).map { Knot(domain.begin.divide(it / knotSize.toDouble(), domain.end)) })

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
