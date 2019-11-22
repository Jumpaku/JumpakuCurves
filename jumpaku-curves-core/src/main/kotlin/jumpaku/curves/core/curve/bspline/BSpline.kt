package jumpaku.curves.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.control.orDefault
import jumpaku.commons.json.ToJson
import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.weighted
import jumpaku.curves.core.transform.Transform


class BSpline private constructor(val nurbs: Nurbs) : Curve by nurbs, Differentiable, ToJson {

    constructor(controlPoints: Iterable<Point>, knotVector: KnotVector) : this(Nurbs(controlPoints.map { it.weighted() }, knotVector))

    val controlPoints: List<Point> = nurbs.controlPoints

    val knotVector: KnotVector = nurbs.knotVector

    val degree: Int = knotVector.degree

    override val domain: Interval = knotVector.domain

    override val derivative: BSplineDerivative by lazy {
        val us = knotVector.extractedKnots
        val cvs = controlPoints
                .zipWithNext { a, b -> b.toCrisp() - a.toCrisp() }
                .mapIndexed { i, v ->
                    v * basisHelper(degree.toDouble(), 0.0, us[degree + i + 1], us[i + 1])
                }

        BSplineDerivative(cvs, knotVector.derivativeKnotVector())
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

    override fun toCrisp(): BSpline = BSpline(nurbs.toCrisp())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlPoints" to jsonArray(controlPoints.map { it.toJson() }),
            "degree" to degree,
            "knots" to jsonArray(knotVector.knots.map { it.toJson() }))

    fun transform(a: Transform): BSpline = BSpline(nurbs.transform(a))

    fun restrict(begin: Double, end: Double): BSpline = BSpline(nurbs.restrict(begin, end))

    fun restrict(i: Interval): BSpline = restrict(i.begin, i.end)

    fun reverse(): BSpline = BSpline(nurbs.reverse())

    /**
     * Multiplies degree + 1 knots at begin and end of domain.
     * Head and last of control points are moved to beginning point and end point of BSpline curve.
     */
    fun clamp(): BSpline = BSpline(nurbs.clamp())

    /**
     * Closes BSpline.
     * Moves head and last of clamped control points to head.middle(last).
     */
    fun close(): BSpline = BSpline(nurbs.close())

    fun toBeziers(): List<Bezier> = nurbs.toRationalBeziers().map{ Bezier(it.controlPoints) }

    fun subdivide(t: Double): Pair<Option<BSpline>, Option<BSpline>> = nurbs.subdivide(t).run {
        Pair(first.map(::BSpline), second.map(::BSpline))
    }

    fun insertKnot(t: Double, times: Int = 1): BSpline = BSpline(nurbs.insertKnot(t, times))

    /**
     * When the multiplicity of t is zero or one,
     * bSpline.toCrisp().insertKnot(t, times).removeKnot(t, times) == bSpline.toCrisp()
     */
    fun removeKnot(t: Double, times: Int = 1): BSpline = BSpline(nurbs.removeKnot(t, times))

    fun removeKnot(knotIndex: Int, times: Int = 1): BSpline = BSpline(nurbs.removeKnot(knotIndex, times))

    companion object {

        fun fromJson(json: JsonElement): BSpline {
            val d = json["degree"].int
            val cp = json["controlPoints"].array.map { Point.fromJson(it) }
            val ks = json["knots"].array.map { Knot.fromJson(it) }
            return BSpline(cp, KnotVector(d, ks))
        }

        fun basis(t: Double, i: Int, knotVector: KnotVector): Double {
            val domain = knotVector.domain
            require(t in knotVector.domain) { "knot($t) is out of domain($domain)." }
            val us = knotVector.extractedKnots
            val (_, e) = domain
            val p = knotVector.degree
            if (t == e) return if (i == us.size - p - 2) 1.0 else 0.0

            val l = knotVector.searchLastExtractedLessThanOrEqualTo(t)
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
