package jumpaku.curves.core.curve.bspline

import jumpaku.commons.control.Option
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.util.asKt
import jumpaku.curves.core.util.asVavr

class Nurbs(
        controlPoints: Iterable<Point>,
        weights: Iterable<Double>,
        val knotVector: KnotVector) : Curve, Differentiable {

    constructor(weightedControlPoints: Iterable<WeightedPoint>, knotVector: KnotVector) : this(
            weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight }, knotVector)

    val controlPoints: List<Point> = controlPoints.toList()

    val weights: List<Double> = weights.toList()

    val degree: Int = knotVector.degree

    val weightedControlPoints: List<WeightedPoint> get() = controlPoints.zip(weights, ::WeightedPoint)

    override val domain: Interval = knotVector.domain

    override fun differentiate(): Derivative {
        val ws = weights
        val dws = ws.zipWithNext { a, b -> degree * (b - a) }
        val dp = BSplineDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }, knotVector).differentiate()

        return object : Derivative {
            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }

                val wt = BSpline(weights.map { Point.x(it) }, knotVector).evaluate(t).x
                val dwt = BSpline(dws.map { Point.x(it) }, knotVector.derivativeKnotVector()).evaluate(t).x
                val dpt = dp.evaluate(t)
                val rt = this@Nurbs.evaluate(t).toVector()

                return ((dpt - dwt * rt) / wt).orThrow()
            }

            override val domain: Interval get() = this@Nurbs.domain
        }
    }

    init {
        val us = knotVector.extractedKnots
        val p = knotVector.degree
        val n = this.controlPoints.size
        val m = us.size
        require(n >= p + 1) { "controlPoints.size()($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size()($m) - degree($p) - 1 != controlPoints.size()($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
    }

    override fun toString(): String = "Nurbs(knotVector=$knotVector, weightedControlPoints=$weightedControlPoints)"

    override fun toCrisp(): Nurbs = Nurbs(controlPoints.map { it.toCrisp() }, weights, knotVector)

    override fun evaluate(t: Double): Point = evaluate(weightedControlPoints, knotVector, t).point

    fun transform(a: Transform): Nurbs = Nurbs(controlPoints.map(a::invoke), weights, knotVector)

    fun restrict(begin: Double, end: Double): Nurbs {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }

        return subdivide(begin).second.orThrow().subdivide(end).first.orThrow()
    }

    fun restrict(i: Interval): Nurbs = restrict(i.begin, i.end)

    fun reverse(): Nurbs = Nurbs(weightedControlPoints.reversed(), knotVector.reverse())

    /**
     * Multiplies more than degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): Nurbs = Nurbs(clampedControlPoints(weightedControlPoints, knotVector), knotVector.clamp())

    fun close(): Nurbs = Nurbs(closedControlPoints(weightedControlPoints, knotVector), knotVector.clamp())

    fun toRationalBeziers(): List<RationalBezier> {
        val (b, e) = domain
        val sb = knotVector.multiplicityOf(b)
        val se = knotVector.multiplicityOf(e)
        return segmentedControlPoints(weightedControlPoints, knotVector)
                .run { slice((degree + 1 - sb) until (size - degree - 1 + se)) }
                .windowed(degree + 1, degree + 1)
                .map { RationalBezier(it) }
    }

    fun subdivide(t: Double): Pair<Option<Nurbs>, Option<Nurbs>> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val (cp0, cp1) = subdividedControlPoints(t, weightedControlPoints, knotVector)
        val (kv0, kv1) = knotVector.subdivide(t)
        return Pair(kv0.map { Nurbs(cp0, it) }, kv1.map { Nurbs(cp1, it) })
    }

    fun insertKnot(t: Double, times: Int = 1): Nurbs {
        require(t in domain) { "t($t) is out of domain($domain)." }
        val s = knotVector.multiplicityOf(t)
        val p = knotVector.degree
        val h = times.coerceIn(0..(p + 1 - s))
        return Nurbs(insertedControlPoints(weightedControlPoints, knotVector, t, h), knotVector.insert(t, h))
    }

    fun removeKnot(t: Double, times: Int = 1): Nurbs {
        val i = knotVector.search(t)
        require(t in domain) { "t($t) is out of domain($domain)." }
        require(i >= 0 || times == 0) { "not found t($t) in $knotVector" }
        return if (times == 0) this
        else Nurbs(removedControlPoints(weightedControlPoints, knotVector, i, times), knotVector.remove(i, times))
    }

    fun removeKnot(knotIndex: Int, times: Int = 1): Nurbs {
        val s = knotVector.knots[knotIndex].multiplicity
        val h = times.coerceIn(0..s)
        return Nurbs(removedControlPoints(weightedControlPoints, knotVector, knotIndex, h), knotVector.remove(knotIndex, h))
    }

    companion object {

        private tailrec fun evaluate(controlPoints: List<WeightedPoint>, knotVector: KnotVector, t: Double): WeightedPoint {
            val us = knotVector.extractedKnots
            val (b, e) = knotVector.domain
            if (b < e && t == e) return evaluate(controlPoints.reversed(), knotVector.reverse(), b)

            val l = knotVector.searchLastExtractedLessThanOrEqualTo(t)
            val d = knotVector.degree
            val result = controlPoints.subList(l - d, l + 1).toMutableList()

            for (k in 1..d) {
                for (i in l downTo (l - d + k)) {
                    val aki = BSpline.basisHelper(t, us[i], us[i + d + 1 - k], us[i])
                    result[i - (l - d)] = result[i - 1 - (l - d)].lerp(aki, result[i - (l - d)])
                }
            }
            return result[l - (l - d)]
        }

        private fun insertedControlPoints(
                controlPoints: List<WeightedPoint>, knotVector: KnotVector, knotValue: Double, times: Int): List<WeightedPoint> {
            val (b, e) = knotVector.domain
            if (b < e && knotValue == e) return insertedControlPoints(
                    controlPoints.reversed(), knotVector.reverse(), b, times).reversed()
            val us = knotVector.extractedKnots
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(knotValue)
            val k = knotVector.searchLastExtractedLessThanOrEqualTo(knotValue)
            var cp = controlPoints.toMutableList()

            for (r in 1..times) {
                val front = cp.take(k - p + r)
                val back = cp.drop(k - s)
                for (i in (k - s) downTo (k - p + r)) {
                    val a = BSpline.basisHelper(knotValue, us[i], us[i + p - r + 1], us[i])
                    cp[i] = cp[i - 1].lerp(a, cp[i])
                }
                cp = (front + cp.slice((k - p + r)..(k - s)) + back).toMutableList()
            }
            return cp
        }

        private fun removedControlPoints(
                controlPoints: List<WeightedPoint>, knotVector: KnotVector, knotIndex: Int, times: Int): List<WeightedPoint> {
            if (times == 0) return controlPoints

            val (u, s) = knotVector.knots[knotIndex]
            val (b, e) = knotVector.domain
            if (b < e && u == e) return removedControlPoints(
                    controlPoints.reversed(), knotVector.reverse(), knotVector.knots.size - knotIndex - 1, times
            ).reversed()

            val p = knotVector.degree
            val k = knotVector.searchLastExtractedLessThanOrEqualTo(u)
            if (s > p) return removedControlPoints(
                    controlPoints.asVavr().removeAt(k - p).asKt(),
                    knotVector.remove(knotIndex, 1), knotIndex, times - 1)

            val us = knotVector.extractedKnots

            val cp = controlPoints.toMutableList()
            for (t in 1..times) {
                var i = k - p - t + 1
                var j = k - s + t - 1
                while (j - i >= t) {
                    val ai = BSpline.basisHelper(u, us[i], us[i + p + t], us[i])
                    cp[i] = cp[i].lerp((ai - 1) / ai, cp[i - 1])
                    ++i
                    val aj = BSpline.basisHelper(u, us[j - t + 1], us[j + p + 1], us[j - t + 1])
                    cp[j] = cp[j].lerp(aj / (aj - 1), cp[j + 1])
                    --j
                }
            }
            val f = k - p - times + 1
            val ff = (2 * k - p - s - times) / 2
            val l = k - s + times - 1
            val ll = (2 * k - p - s + times + 2) / 2

            return cp.take(f) + cp.slice(f..ff) + cp.slice(ll..l) + cp.drop(l + 1)
        }

        private fun subdividedControlPoints(
                t: Double, controlPoints: List<WeightedPoint>, knotVector: KnotVector
        ): Pair<List<WeightedPoint>, List<WeightedPoint>> {
            val p = knotVector.degree
            val s = knotVector.multiplicityOf(t)
            val times = p + 1 - s
            val (b, e) = knotVector.domain
            if (b < e && t == e)
                return subdividedControlPoints(b, controlPoints.reversed(), knotVector.reverse())
                        .let { (x, y) -> Pair(y.reversed(), x.reversed()) }

            val k = knotVector.searchLastExtractedLessThanOrEqualTo(t)
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
            return Pair(front.toList(), back.toList())
        }

        private fun segmentedControlPoints(controlPoints: List<WeightedPoint>, knotVector: KnotVector): List<WeightedPoint> {
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

        private fun clampedControlPoints(controlPoints: List<WeightedPoint>, knotVector: KnotVector): List<WeightedPoint> {
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

        private fun closedControlPoints(controlPoints: List<WeightedPoint>, knotVector: KnotVector): List<WeightedPoint> {
            val cp = clampedControlPoints(controlPoints, knotVector)
            val closeAt = cp.run { first().middle(last()) }
            return cp.asVavr().update(0, closeAt).update(cp.lastIndex, closeAt).asKt()
        }
    }
}
