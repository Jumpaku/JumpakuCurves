package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.*
import jumpaku.core.transform.Transform
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthReparameterized
import jumpaku.core.curve.arclength.approximate
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.divOption
import jumpaku.core.util.lastIndex
import org.apache.commons.math3.util.Precision


class BSpline(val controlPoints: Array<Point>, val knotVector: KnotVector)
    : Curve, Differentiable, ToJson {

    val degree: Int = knotVector.degree

    override val domain: Interval = knotVector.domain

    override val derivative: BSplineDerivative get() {
        val us = knotVector.extractedKnots
        val cvs = controlPoints
                .zipWith(controlPoints.tail()) { a, b -> b.toCrisp() - a.toCrisp() }
                .zipWithIndex({ v, i ->
                    v* basisHelper(degree.toDouble(), 0.0, us[degree + i + 1], us[i + 1])
                })

        return BSplineDerivative(cvs, knotVector.derivativeKnotVector())
    }

    init {
        val us = knotVector.extractedKnots
        val p = knotVector.degree
        val n = controlPoints.size()
        val m = us.size()
        require(n >= p + 1) { "controlPoints.size()($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size()($m) - degree($p) - 1 != controlPoints.size()($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
        require(domain.begin < domain.end) { "domain.begin(${domain.begin}) < domain.end(${domain.end})" }
    }

    constructor(controlPoints: Iterable<Point>, knotVector: KnotVector) : this(Array.ofAll(controlPoints), knotVector)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

    override val reparameterized: ArcLengthReparameterized by lazy {
        approximate(clamp(),
                {
                    val cp = (it as BSpline).controlPoints
                    val l0 = Polyline(cp).reparametrizeArcLength().arcLength()
                    val l1 = cp.run { head().dist(last()) }
                    !Precision.equals(l0, l1, 1.0 / 256)
                },
                { b, i: Interval -> (b as BSpline).restrict(i) })
    }

    override fun toCrisp(): BSpline = BSpline(controlPoints.map { it.toCrisp() }, knotVector)

    override fun evaluate(t: Double): Point = evaluate(controlPoints, knotVector, t)

    override fun differentiate(t: Double): Vector = derivative.evaluate(t)

    fun transform(a: Transform): BSpline = BSpline(controlPoints.map(a::invoke), knotVector)

    fun restrict(begin: Double, end: Double): BSpline {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin)._2().get().subdivide(end)._1().get()
    }

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(controlPoints.reverse(), knotVector.reverse())

    /**
     * Multiplies degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): BSpline = restrict(domain)//BSpline(clampedControlPoints(controlPoints, knotVector), knotVector.clamp())

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline = BSpline(closedControlPoints(controlPoints, knotVector), knotVector.clamp())

    fun toBeziers(): Array<Bezier> {
        val (b, e) = domain
        val sb = knotVector.multiplicityOf(b)
        val se = knotVector.multiplicityOf(e)
        return segmentedControlPoints(controlPoints, knotVector)
                .run { slice(degree + 1 - sb, size() - degree - 1 + se) }
                .sliding(degree + 1, degree + 1)
                .map { Bezier(it) }
                .toArray()
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
        if (times == 0) return this
        return BSpline(insertedControlPoints(controlPoints, knotVector, t, times), knotVector.insert(t, times))
    }

    fun removeKnot(t: Double, times: Int = 1): BSpline {
        val i = knotVector.search(t)
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(i >= 0 || times == 0) { "not found t($t) in $knotVector" }
        return if (times == 0) this
        else BSpline(removedControlPoints(controlPoints, knotVector, i, times), knotVector.remove(i, times))
    }

    companion object {

        fun fromJson(json: JsonElement): Option<BSpline> = Try.ofSupplier {
            BSpline(json["controlPoints"].array.flatMap { Point.fromJson(it) }, KnotVector.fromJson(json["knotVector"]).get())
        }.toOption()

        tailrec fun <D : Divisible<D>> evaluate(controlPoints: Array<D>, knotVector: KnotVector, t: Double): D {
            val us = knotVector.extractedKnots
            val (b, e) = knotVector.domain
            if (b < e && t == e) return evaluate(controlPoints.reverse(), knotVector.reverse(), b)

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

        fun <D : Divisible<D>> insertedControlPoints(controlPoints: Array<D>, knotVector: KnotVector, knotValue: Double, times: Int): Array<D> {
            val (b, e) = knotVector.domain
            if(b < e && knotValue == e) return insertedControlPoints(
                    controlPoints.reverse(), knotVector.reverse(), b, times).reverse()
            val us = knotVector.extractedKnots
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(knotValue)
            val k = knotVector.lastExtractedIndexUnder(knotValue)
            var cp = controlPoints.toJavaList()

            for (r in 1..times) {
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
            if (times == 0) return controlPoints

            val (u, s) = knotVector.knots[knotIndex]
            val (b, e) = knotVector.domain
            if (b < e && u == e) return removedControlPoints(
                    controlPoints.reverse(), knotVector.reverse(), knotVector.knots.size() - knotIndex - 1, times).reverse()

            val p = knotVector.degree
            val k = knotVector.lastExtractedIndexUnder(u)
            if (s > p) return removedControlPoints(
                    controlPoints.removeAt(k - p), knotVector.remove(knotIndex, 1), knotIndex, times - 1)

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

            return Array.ofAll(cp.take(f) + cp.slice(f..ff) + cp.slice(ll..l) + cp.drop(l + 1))
        }

        fun <D : Divisible<D>> subdividedControlPoints(t: Double, controlPoints: Array<D>, knotVector: KnotVector): Tuple2<Array<D>, Array<D>> {
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(t)
            val times = p + 1 - s
            val (b, e) = knotVector.domain
            if (b < e && t == e)
                return subdividedControlPoints(b, controlPoints.reverse(), knotVector.reverse())
                        .let { (x, y) -> Tuple2(y.reverse(), x.reverse()) }

            val k = knotVector.lastExtractedIndexUnder(t)
            val cp = insertedControlPoints(controlPoints, knotVector, t, times)
            val i = k - p + times
            val front = if (b < e && t == b) cp.take(i).appendAll(Array.fill(s) { cp[i] }) else if (t == e) cp.dropRight(times) else cp.take(i)
            val back = if (b < e && t == e) cp.takeRight(times).prependAll(Array.fill(s) { cp[cp.lastIndex - times] }) else cp.drop(i)
            return Tuple2(front, back)
        }

        fun <D : Divisible<D>> segmentedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
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

        fun <D : Divisible<D>> clampedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
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
            return tmpControlPoints.run { drop(bTimes).take(controlPoints.size()) }
        }

        fun <D : Divisible<D>> closedControlPoints(controlPoints: Array<D>, knotVector: KnotVector): Array<D> {
            val cp = clampedControlPoints(controlPoints, knotVector)
            val closeAt = cp.run { head().middle(last()) }
            return cp.update(0, closeAt).update(cp.lastIndex, closeAt)
        }

        fun basis(t: Double, i: Int, knotVector: KnotVector): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            val us = knotVector.extractedKnots
            val (_, e) = domain
            val p = knotVector.degree
            if (t == e) return if (i == us.size() - p - 2) 1.0 else 0.0

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

        fun basisHelper(a: Double, b: Double, c: Double, d: Double): Double = (a - b).divOption (c - d).getOrElse(0.0)
    }
}
