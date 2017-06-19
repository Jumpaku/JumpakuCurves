package org.jumpaku.core.curve.rationalbezier


import io.vavr.API.Array
import io.vavr.API.Stream
import io.vavr.collection.Array
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.json.prettyGson


class InterpolatingConicSection(
        val begin: Point, val middle: Point, val end: Point, val weight: Double) : FuzzyCurve, Differentiable, CrispTransformable {

    val asCrispRationalBezier: RationalBezier get() {
        require((1.0 / weight).isFinite()) { "weight is zero." }

        return RationalBezier(Stream(
                begin.toCrisp(),
                Crisp(-0.5 / weight * begin.toVector() + (1 + 1 / weight) * middle.toVector() - 0.5 / weight * end.toVector()),
                end.toCrisp()
        ).zipWith(Stream(1.0, weight, 1.0), ::WeightedPoint))
    }

    val representPoints: Array<Point> get() = Array(begin, middle, end)

    val degree = 2

    override val domain: Interval get() = Interval.ZERO_ONE

    override val derivative: Derivative get() = asCrispRationalBezier.derivative

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): InterpolatingConicSectionJson = InterpolatingConicSectionJson(this)

    override fun differentiate(t: Double): Vector = asCrispRationalBezier.differentiate(t)

    override fun sampleArcLength(n: Int): Array<Point> = Polyline.approximate(this).sampleArcLength(n)

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val r0 = representPoints[0].r
        val r1 = representPoints[1].r
        val r2 = representPoints[2].r
        val wt = RationalBezier.bezier1D(t, Array(1.0, weight, 1.0))
        val r = FastMath.abs(r0 * (1 - t) * (1 - 2 * t) / wt) +
                FastMath.abs(r1 * 2 * (weight + 1) * t * (1 - t) / wt) +
                FastMath.abs(r2 * t * (2 * t - 1) / wt)
        val p = asCrispRationalBezier.evaluate(t)

        return Fuzzy(p.toCrisp(), r)
    }

    override fun crispTransform(a: Transform): InterpolatingConicSection = InterpolatingConicSection(
            a(begin.toCrisp()), a(middle.toCrisp()), a(end.toCrisp()), weight)

    fun reverse(): InterpolatingConicSection = InterpolatingConicSection(end, middle, begin, weight)

    fun complement(): InterpolatingConicSection = InterpolatingConicSection(begin, middle, end, -weight)
}

data class InterpolatingConicSectionJson(
        private val begin: PointJson,
        private val middle: PointJson,
        private val end: PointJson,
        private val weight: Double) {

    constructor(interpolatingConicSection: InterpolatingConicSection) : this(
            interpolatingConicSection.begin.json(),
            interpolatingConicSection.middle.json(),
            interpolatingConicSection.end.json(),
            interpolatingConicSection.weight)

    fun interpolatingConicSection(): InterpolatingConicSection = InterpolatingConicSection(
                begin.point(), middle.point(), end.point(), weight)
}
