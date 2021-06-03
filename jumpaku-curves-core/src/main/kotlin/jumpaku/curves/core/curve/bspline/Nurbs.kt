package jumpaku.curves.core.curve.bspline

import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.RationalBezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.WeightedPoint
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.transform.Transform
import java.lang.Math.min
import kotlin.math.max

class Nurbs(
    controlPoints: List<Point>,
    weights: List<Double>,
    val knotVector: KnotVector
) : Curve, Differentiable {

    constructor(weightedControlPoints: List<WeightedPoint>, knotVector: KnotVector) : this(
        weightedControlPoints.map { it.point }, weightedControlPoints.map { it.weight }, knotVector
    )

    val controlPoints: List<Point> = controlPoints.toList()

    val weights: List<Double> = weights.toList()

    val degree: Int = knotVector.degree

    val weightedControlPoints: List<WeightedPoint> = controlPoints.zip(weights, ::WeightedPoint)

    override val domain: Interval = knotVector.domain

    init {
        val us = knotVector
        val p = knotVector.degree
        val n = this.controlPoints.size
        val m = us.size
        require(n >= p + 1) { "controlPoints.size($n) < degree($p) + 1" }
        require(m - p - 1 == n) { "knotVector.size($m) - degree($p) - 1 != controlPoints.size($n)" }
        require(degree > 0) { "degree($degree) <= 0" }
    }

    override fun differentiate(): Derivative {
        val ws = weights
        val dws = ws.zipWithNext { a, b -> degree * (b - a) }
        val dwt = BSpline(dws.map { Point.x(it) }, knotVector.differentiate())
        val wt = BSpline(weights.map { Point.x(it) }, knotVector)
        val dp = BSplineDerivative(weightedControlPoints.map { (p, w) -> p.toVector() * w }, knotVector).differentiate()
        return object : Derivative {

            override fun evaluate(t: Double): Vector {
                require(t in domain) { "t($t) is out of domain($domain)" }
                if (domain.begin == domain.end) return Vector.Zero
                val dpt = dp.evaluate(t)
                val rt = this@Nurbs.evaluate(t).toVector()
                return ((dpt - dwt.evaluate(t).x * rt) / wt.evaluate(t).x).orThrow()
            }

            override val domain: Interval get() = this@Nurbs.domain
        }
    }

    override fun toString(): String = "Nurbs(knotVector=$knotVector, weightedControlPoints=$weightedControlPoints)"

    override fun toCrisp(): Nurbs = Nurbs(controlPoints.map { it.toCrisp() }, weights, knotVector)

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        if (t == domain.end) return controlPoints.last()
        val l = knotVector.searchIndexToInsert(t)
        val result = deboor(weightedControlPoints, knotVector, t, l)
        return result.point
    }

    override fun evaluateAll(sortedParams: List<Double>): List<Point> {
        if (domain.begin == domain.end)
            return sortedParams.map { controlPoints.last() }
        var knotIndex = degree
        val results = ArrayList<Point>(sortedParams.size)
        for (t in sortedParams) {
            if (sortedParams.size < knotVector.size) {
                knotIndex = knotVector.searchIndexToInsert(t).coerceAtMost(controlPoints.lastIndex)
            } else {
                while (knotIndex < controlPoints.lastIndex && t >= knotVector[knotIndex + 1]) ++knotIndex
            }
            val eval = deboor(weightedControlPoints, knotVector, t, knotIndex)
            results.add(eval.point)
        }
        return results
    }

    fun transform(a: Transform): Nurbs = Nurbs(controlPoints.map(a), weights, knotVector)

    fun restrict(begin: Double, end: Double): Nurbs {
        require(Interval(begin, end) in domain) { "Interval([$begin, $end]) is out of domain($domain)" }
        return subdivide(begin).second.subdivide(end).first
    }

    fun restrict(i: Interval): Nurbs = restrict(i.begin, i.end)

    fun reverse(): Nurbs = Nurbs(controlPoints.asReversed(), weights.asReversed(), knotVector)


    fun close(): Nurbs {
        val cp = weightedControlPoints.let { cp ->
            val closeAt = cp.run { first().middle(last()) }
            listOf(closeAt) + cp.subList(1, cp.lastIndex) + listOf(closeAt)
        }
        return Nurbs(cp, knotVector)
    }

    fun toRationalBeziers(): List<RationalBezier> {
        if (domain.begin == domain.end) return listOf(RationalBezier(weightedControlPoints))
        val cp = ArrayList(weightedControlPoints)
        val us = ArrayList(knotVector)
        val results = mutableListOf<RationalBezier>()
        var i = controlPoints.lastIndex
        while (i > degree) {
            val u = us[i]
            val (_, points) = deboorWithIntermediate(cp, us, u, i)
            cp.removeAt(cp.lastIndex)
            repeat(degree) { j ->
                cp[cp.size - degree + j] = points[j].first()
            }
            us.removeAt(us.lastIndex)
            repeat(degree) { j ->
                us[us.size - degree + j] = u
            }
            while (us[i - 1] == u) {
                us.removeAt(us.lastIndex)
                cp.removeAt(cp.lastIndex)
                --i
            }
            i -= 1
            results += RationalBezier(points.map { it.last() }.asReversed())
        }
        return (results + RationalBezier(cp)).reversed()
    }

    fun subdivide(t: Double): Pair<Nurbs, Nurbs> {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val wcp = weightedControlPoints
        val (b, e) = domain
        if (t == b) return Pair(
            Nurbs(
                List(degree + 1) { wcp.first() },
                KnotVector.clamped(Interval(t, t), degree, degree * 2 + 2)
            ), this
        )
        if (t == e) return Pair(
            this,
            Nurbs(
                List(degree + 1) { wcp.last() },
                KnotVector.clamped(Interval(t, t), degree, degree * 2 + 2)
            )
        )
        val l = knotVector.searchIndexToInsert(t)
        val (_, intermediate) = deboorWithIntermediate(wcp, knotVector, t, l)
        val front = wcp.take(l - degree)
        val middle0 = intermediate.map { it.first() }
        val middle1 = intermediate.map { it.last() }.reversed()
        val back = wcp.drop(l + 1)
        val (kv0, kv1) = knotVector.subdivide(t)
        val cp0 = (front + middle0).take(kv0.size - degree - 1)
        val cp1 = (middle1 + back).takeLast(kv1.size - degree - 1)
        return Pair(Nurbs(cp0, kv0), Nurbs(cp1, kv1))
    }

    fun insertKnot(t: Double, times: Int = 1): Nurbs {
        require(t in domain) { "${t::class.java.name}t($t) is out of domain($domain)." }
        require(times >= 0) { "times($times) must be non negative." }
        if (t == domain.end || t == domain.begin) return this
        val l = knotVector.searchIndexToInsert(t)
        val wcp = weightedControlPoints
        val (point, intermediate) = deboorWithIntermediate(wcp, knotVector, t, l)
        val front = wcp.take(l - degree)
        val middle0 = intermediate.map { it.first() }.take(times)
        val middle1 = intermediate[min(times, degree)] + List(max(0, times - degree)) { point }
        val middle2 = intermediate.map { it.last() }.take(times).reversed()
        val back = wcp.drop(l + 1)
        return Nurbs(front + middle0 + middle1 + middle2 + back, knotVector.insertKnot(t, times))
    }

    companion object {

        internal fun deboor(
            controlPoints: List<WeightedPoint>,
            knots: List<Double>,
            t: Double,
            indexToInsert: Int
        ): WeightedPoint {
            val (p, _) = deboorImpl(controlPoints, knots, t, indexToInsert, false)
            return p
        }

        private fun deboorWithIntermediate(
            controlPoints: List<WeightedPoint>,
            knots: List<Double>,
            t: Double,
            indexToInsert: Int
        ): Pair<WeightedPoint, List<List<WeightedPoint>>> {
            val (p, i) = deboorImpl(controlPoints, knots, t, indexToInsert, true)
            return Pair(p, i!!)
        }

        private fun deboorImpl(
            controlPoints: List<WeightedPoint>,
            knots: List<Double>,
            t: Double,
            indexToInsert: Int,
            storesIntermediate: Boolean
        ): Pair<WeightedPoint, List<List<WeightedPoint>>?> {
            val d = knots.size - controlPoints.size - 1
            val idx = indexToInsert
            val result = controlPoints.subList(idx - d, idx + 1).toMutableList()
            val store = if (storesIntermediate) mutableListOf(result.toList()) else null
            for (k in 1..d) {
                for (i in idx downTo (idx - d + k)) {
                    val aki = BSpline.basisHelper(t, knots[i], knots[i + d + 1 - k], knots[i])
                    result[i - (idx - d)] = result[i - 1 - (idx - d)].lerp(aki, result[i - (idx - d)])
                }
                store?.add(result.subList(k, result.size).toList())
            }
            return Pair(result[d], store)
        }
    }
}
