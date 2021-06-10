package jumpaku.curves.core.curve.bspline

import jumpaku.commons.math.divOrDefault
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.weighted
import jumpaku.curves.core.transform.Transform


class BSpline private constructor(val nurbs: Nurbs) : Curve by nurbs, Differentiable {

    constructor(
        controlPoints: List<Point>,
        knotVector: KnotVector
    ) : this(Nurbs(controlPoints.map { it.weighted() }, knotVector))

    val controlPoints: List<Point> = nurbs.controlPoints

    val knotVector: KnotVector = nurbs.knotVector

    val degree: Int = knotVector.degree

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

    override fun differentiate(): BSplineDerivative {
        val us = knotVector
        val cvs = controlPoints
            .zipWithNext { a, b -> b - a }
            .mapIndexed { i, v ->
                v * basisHelper(degree.toDouble(), 0.0, us[degree + i + 1], us[i + 1])
            }

        return BSplineDerivative(cvs, knotVector.differentiate())
    }

    override fun toCrisp(): BSpline = BSpline(nurbs.toCrisp())

    override fun toString(): String = "BSpline(knotVector=$knotVector, controlPoints=$controlPoints)"

    fun transform(a: Transform): BSpline = BSpline(nurbs.transform(a))

    override fun restrict(begin: Double, end: Double): BSpline = BSpline(nurbs.restrict(begin, end))

    override fun restrict(subDomain: Interval): BSpline = restrict(subDomain.begin, subDomain.end)

    fun reverse(): BSpline = BSpline(nurbs.reverse())

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline = BSpline(nurbs.close())

    fun toBeziers(): List<Bezier> = nurbs.toRationalBeziers().map { Bezier(it.controlPoints) }

    fun subdivide(t: Double): Pair<BSpline, BSpline> = nurbs.subdivide(t).run { Pair(BSpline(first), BSpline(second)) }

    fun insertKnot(t: Double, times: Int = 1): BSpline = BSpline(nurbs.insertKnot(t, times))

    companion object {

        fun basis(t: Double, i: Int, knotVector: KnotVector): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            if (t == domain.end) return if (i == knotVector.size - knotVector.degree - 2) 1.0 else 0.0
            val l = knotVector.searchIndexToInsert(t)
            return basis(t, i, knotVector, l)
        }

        fun basis(t: Double, i: Int, knotVector: KnotVector, l: Int): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            val us = knotVector
            val (_, e) = domain
            val p = knotVector.degree
            if (t == e) return if (i == us.size - p - 2) 1.0 else 0.0

            val ns = (0..p).map { index -> if (index == l - i) 1.0 else 0.0 }.toMutableList()

            for (j in 1..(ns.lastIndex)) {
                for (k in 0..(ns.lastIndex - j)) {
                    val left = basisHelper(t, us[k + i], us[k + i + j], us[k + i])
                    val right = basisHelper(us[k + 1 + i + j], t, us[k + 1 + i + j], us[k + 1 + i])
                    ns[k] = left * ns[k] + right * ns[k + 1]
                }
            }

            return ns[0]
        }

        internal fun basisHelper(a: Double, b: Double, c: Double, d: Double): Double =
            (a - b).divOrDefault(c - d) { 0.0 }
    }
}


