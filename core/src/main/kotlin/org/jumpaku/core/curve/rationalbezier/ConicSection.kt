package org.jumpaku.core.curve.rationalbezier


import io.vavr.API.Array
import io.vavr.API.Stream
import io.vavr.collection.Array
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.json.prettyGson


class ConicSection(
        val begin: Point, val far: Point, val end: Point, val weight: Double) : FuzzyCurve, Differentiable, CrispTransformable {

    val asCrispRationalBezier: RationalBezier get() {
        if(!(1.0 / weight).isFinite()) {
            return RationalBezier(
                    WeightedPoint(begin), WeightedPoint(begin.divide(0.5, end)), WeightedPoint(end))
        }

        return RationalBezier(Stream(
                begin.toCrisp(),
                far.divide(-1/weight, begin.divide(0.5, end)).toCrisp(),
                end.toCrisp()
        ).zipWith(Stream(1.0, weight, 1.0), ::WeightedPoint))
    }

    val representPoints: Array<Point> get() = Array(begin, far, end)

    val degree = 2

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: Derivative get() = asCrispRationalBezier.derivative

    init {
        require(-1.0 < weight && weight < 1.0) { "weight($weight) is out of (-1.0, 1.0)" }
    }

    override fun differentiate(t: Double): Vector = asCrispRationalBezier.differentiate(t)

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val wt = RationalBezier.bezier1D(t, Array(1.0, weight, 1.0))

        val p0 = begin.toVector()
        val p1 = far.toVector()
        val p2 = end.toVector()
        val p = (1/wt)*((1-t)*(1-2*t)*p0 + 2*t*(1-t)*(1+weight)*p1 + t*(2*t-1)*p2)
        val r0 = representPoints[0].r
        val r1 = representPoints[1].r
        val r2 = representPoints[2].r
        val r = FastMath.abs(r0 * (1 - t) * (1 - 2 * t) / wt) +
                FastMath.abs(r1 * 2 * (weight + 1) * t * (1 - t) / wt) +
                FastMath.abs(r2 * t * (2 * t - 1) / wt)

        return Fuzzy(p, r)
    }

    override fun crispTransform(a: Transform): ConicSection = ConicSection(
            a(begin.toCrisp()), a(far.toCrisp()), a(end.toCrisp()), weight)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): ConicSectionJson = ConicSectionJson(this)

    fun reverse(): ConicSection = ConicSection(end, far, begin, weight)

    fun complement(): ConicSection = ConicSection(begin, center().divide(-1.0, far), end, -weight)

    fun center(): Point = begin.divide(0.5, end).divide(weight/(weight - 1), far)
}

data class ConicSectionJson(
        private val begin: PointJson,
        private val far: PointJson,
        private val end: PointJson,
        private val weight: Double) {

    constructor(conicSection: ConicSection) : this(
            conicSection.begin.json(),
            conicSection.far.json(),
            conicSection.end.json(),
            conicSection.weight)

    fun conicSection(): ConicSection = ConicSection(
                begin.point(), far.point(), end.point(), weight)
}
