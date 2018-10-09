package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Differentiable
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.geom.Divisible
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.util.*


class BSpline(controlPoints: Iterable<Point>, val knotVector: KnotVector) : Curve, Differentiable, ToJson {

    val controlPoints: List<Point> = controlPoints.toList()

    val degree: Int = knotVector.degree

    override val domain: Interval = knotVector.domain

    override val derivative: BSplineDerivative get() {
        val us = knotVector.extractedKnots
        val cvs = controlPoints
                .zip(controlPoints.asVavr().tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .withIndex().map { (i, v) ->
                    v*basisHelper(degree.toDouble(), 0.0, us[degree + i + 1], us[i + 1])
                }

        return BSplineDerivative(cvs, knotVector.derivativeKnotVector())
    }

    init {
        val us = knotVector.extractedKnots
        val p = knotVector.degree
        val n = this.controlPoints.size
        val m = us.size
        require(n >= p + 1) { "controlPoints.size()($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size()($m) - degree($p) - 1 != controlPoints.size()($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
        require(domain.begin < domain.end) { "domain.begin(${domain.begin}) < domain.end(${domain.end})" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

    override fun toCrisp(): BSpline = BSpline(controlPoints.map { it.toCrisp() }, knotVector)

    override fun evaluate(t: Double): Point = evaluate(controlPoints, knotVector, t)

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    fun transform(a: Transform): BSpline = BSpline(controlPoints.map(a::invoke), knotVector)

    fun restrict(begin: Double, end: Double): BSpline {
        require(begin < end) { "must be begin($begin) < end($end)" }
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().orThrow().subdivide(end)._1().orThrow()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(controlPoints.reversed(), knotVector.reverse())

    /**
     * Multiplies degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): BSpline = BSpline(clampedControlPoints(controlPoints, knotVector), knotVector.clamp())

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline = BSpline(closedControlPoints(controlPoints, knotVector), knotVector.clamp())

    fun toBeziers(): List<Bezier> {
        val (b, e) = domain
        val sb = knotVector.multiplicityOf(b)
        val se = knotVector.multiplicityOf(e)
        return segmentedControlPoints(controlPoints, knotVector)
                .run { slice((degree + 1 - sb) until (size - degree - 1 + se)) }
                .chunked(degree + 1) { Bezier(it) }
    }

    fun subdivide(t: Double): Tuple2<Option<BSpline>, Option<BSpline>> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val (cp0, cp1) = subdividedControlPoints(t, controlPoints, knotVector)
        val (kv0, kv1) = knotVector.subdivide(t)
        return Tuple2(kv0.map { BSpline(cp0, it) }, kv1.map { BSpline(cp1, it) })
    }

    fun insertKnot(t: Double, times: Int = 1): BSpline {
        val p = knotVector.degree
        val s = knotVector.multiplicityOf(t)
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(s + times in 0..(p + 1)) { "multiplicity of t($s) + insertion times($times) must be in [0, degree($p) + 1]" }
        return if (times == 0) this
        else BSpline(insertedControlPoints(controlPoints, knotVector, t, times), knotVector.insert(t, times))
    }

    fun removeKnot(t: Double, times: Int = 1): BSpline {
        val i = knotVector.search(t)
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(i >= 0 || times == 0) { "not found t($t) in $knotVector" }
        return if (times == 0) this
        else BSpline(removedControlPoints(controlPoints, knotVector, i, times), knotVector.remove(i, times))
    }

    companion object {

        fun fromJson(json: JsonElement): Result<BSpline> = result {
            BSpline(json["controlPoints"].array.flatMap { Point.fromJson(it).value() },
                    KnotVector.fromJson(json["knotVector"]).orThrow())
        }

        tailrec fun <D : Divisible<D>> evaluate(controlPoints: List<D>, knotVector: KnotVector, t: Double): D {
            val us = knotVector.extractedKnots
            val (b, e) = knotVector.domain
            if (b < e && t == e) return evaluate(controlPoints.reversed(), knotVector.reverse(), b)

            val l = knotVector.lastExtractedIndexUnder(t)

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

        fun <D : Divisible<D>> insertedControlPoints(
                controlPoints: List<D>, knotVector: KnotVector, knotValue: Double, times: Int): List<D> {
            val (b, e) = knotVector.domain
            if(b < e && knotValue == e) return insertedControlPoints(
                    controlPoints.reversed(), knotVector.reverse(), b, times).reversed()
            val us = knotVector.extractedKnots
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(knotValue)
            val k = knotVector.lastExtractedIndexUnder(knotValue)
            var cp = controlPoints.toMutableList()

            for (r in 1..times) {
                val front = cp.take(k - p + r)
                val back = cp.drop(k - s)
                for (i in (k - s) downTo (k - p + r)) {
                    val a = basisHelper(knotValue, us[i], us[i + p - r + 1], us[i])
                    cp[i] = cp[i - 1].divide(a, cp[i])
                }
                cp = (front + cp.slice((k - p + r)..(k - s)) + back).toMutableList()
            }
            return cp
        }

        fun <D : Divisible<D>> removedControlPoints(
                controlPoints: List<D>, knotVector: KnotVector, knotIndex: Int, times: Int): List<D> {
            if (times == 0) return controlPoints

            val (u, s) = knotVector.knots[knotIndex]
            val (b, e) = knotVector.domain
            if (b < e && u == e) return removedControlPoints(
                    controlPoints.reversed(), knotVector.reverse(), knotVector.knots.size - knotIndex - 1, times
            ).reversed()

            val p = knotVector.degree
            val k = knotVector.lastExtractedIndexUnder(u)
            if (s > p) return removedControlPoints(
                    controlPoints.asVavr().removeAt(k - p).asKt(),
                    knotVector.remove(knotIndex, 1), knotIndex, times - 1)

            val us = knotVector.extractedKnots

            val cp = controlPoints.toMutableList()
            for (t in 1..times) {
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
            val f = k - p - times + 1
            val ff = (2*k - p - s - times)/2
            val l = k - s + times - 1
            val ll = (2*k - p - s + times + 2)/2

            return cp.take(f) + cp.slice(f..ff) + cp.slice(ll..l) + cp.drop(l + 1)
        }

        fun <D : Divisible<D>> subdividedControlPoints(
                t: Double, controlPoints: List<D>, knotVector: KnotVector): Tuple2<List<D>, List<D>> {
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(t)
            val times = p + 1 - s
            val (b, e) = knotVector.domain
            if (b < e && t == e)
                return subdividedControlPoints(b, controlPoints.reversed(), knotVector.reverse())
                        .let { (x, y) -> Tuple2(y.reversed(), x.reversed()) }

            val k = knotVector.lastExtractedIndexUnder(t)
            val cp = insertedControlPoints(controlPoints, knotVector, t, times)
            val i = k - p + times
            val front = when {
                b < e && t == b -> cp.take(i) + List(s) { cp[i] }
                t == e -> cp.dropLast(times)
                else -> cp.take(i)
            }
            val back = when {
                b < e && t == e -> List(s) { cp[cp.lastIndex - times] } + cp.takeLast(times)
                else -> cp.drop(i)
            }
            return Tuple2(front.toList(), back.toList())
        }

        fun <D : Divisible<D>> segmentedControlPoints(controlPoints: List<D>, knotVector: KnotVector): List<D> {
            val p = knotVector.degree
            var tmpControlPoints = controlPoints
            var tmpKnots = knotVector
            knotVector.knots.filter { it.value in knotVector.domain }.forEach { (u, s) ->
                val times = p + 1 - s
                tmpControlPoints = insertedControlPoints(tmpControlPoints, tmpKnots, u, times)
                tmpKnots = tmpKnots.insert(u, times)
            }
            return tmpControlPoints
        }

        fun <D : Divisible<D>> clampedControlPoints(controlPoints: List<D>, knotVector: KnotVector): List<D> {
            val p = knotVector.degree
            var tmpControlPoints = controlPoints
            var tmpKnots = knotVector
            val (b, e) = knotVector.domain
            val bTimes = p + 1 - knotVector.multiplicityOf(b)

            knotVector.knots.filter { it.value == b || it.value == e }.forEach { (u, s) ->
                val times = p + 1 - s
                tmpControlPoints = insertedControlPoints(tmpControlPoints, tmpKnots, u, times)
                tmpKnots = tmpKnots.insert(u, times)
            }
            return tmpControlPoints.run { drop(bTimes).take(controlPoints.size) }
        }

        fun <D : Divisible<D>> closedControlPoints(controlPoints: List<D>, knotVector: KnotVector): List<D> {
            val cp = clampedControlPoints(controlPoints, knotVector)
            val closeAt = cp.run { first().middle(last()) }
            return cp.asVavr().update(0, closeAt).update(cp.lastIndex, closeAt).asKt()
        }

        fun basis(t: Double, i: Int, knotVector: KnotVector): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            val us = knotVector.extractedKnots
            val (_, e) = domain
            val p = knotVector.degree
            if (t == e) return if (i == us.size - p - 2) 1.0 else 0.0

            val l = knotVector.lastExtractedIndexUnder(t)
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

        fun basisHelper(a: Double, b: Double, c: Double, d: Double): Double = (a - b).divOption (c - d).orDefault(0.0)
    }
}
