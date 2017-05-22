package org.jumpaku.curve.rationalbezier


import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.collection.Array
import io.vavr.control.Option
import org.jumpaku.curve.Derivative
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.FuzzyCurve
import org.jumpaku.curve.Interval
import org.apache.commons.math3.util.FastMath
import org.jumpaku.affine.*
import org.jumpaku.curve.polyline.Polyline
import org.jumpaku.json.prettyGson


class InterpolatingConicSection(val begin: Point, val middle: Point, val end: Point, val weight: Double) : FuzzyCurve, Differentiable {

    val asCrispRationalBezier: RationalBezier get() {
        if (!java.lang.Double.isFinite(1.0 / weight)) {
            throw IllegalArgumentException("weight is zero.")
        }

        return RationalBezier(Stream(
                begin.toCrisp(),
                Crisp(-0.5 / weight * begin.toVector() + (1 + 1 / weight) * middle.toVector() - 0.5 / weight * end.toVector()),
                end.toCrisp())
                .zipWith(Stream(1.0, weight, 1.0), ::WeightedPoint))
    }

    val representPoints: Array<Point> get() = Array(begin, middle, end)

    val degree = 2

    override val domain: Interval get() = Interval.ZERO_ONE

    override val derivative: Derivative get() = asCrispRationalBezier.derivative

    override fun toString(): String = InterpolatingConicSectionJson.toJson(this)

    override fun differentiate(t: Double): Vector = asCrispRationalBezier.differentiate(t)

    override fun sampleArcLength(n: Int): Array<Point> = Polyline.approximate(this).sampleArcLength(n)

    override fun evaluate(t: Double): Point {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain$domain")
        }

        val r0 = representPoints[0].r
        val r1 = representPoints[1].r
        val r2 = representPoints[2].r
        val wt = RationalBezier.weightBezier(t, Array(1.0, weight, 1.0))
        val r = FastMath.abs(r0 * (1 - t) * (1 - 2 * t) / wt) +
                FastMath.abs(r1 * 2 * (weight + 1) * t * (1 - t) / wt) +
                FastMath.abs(r2 * t * (2 * t - 1) / wt)
        val p = asCrispRationalBezier.evaluate(t)

        return Fuzzy(p.toCrisp(), r)
    }

    fun reverse(): InterpolatingConicSection = InterpolatingConicSection(end, middle, begin, weight)

    fun complement(): InterpolatingConicSection = InterpolatingConicSection(begin, middle, end, -weight)
}

data class InterpolatingConicSectionJson(
        val begin: PointJson,
        val middle: PointJson,
        val end: PointJson,
        val weight: Double) {

    companion object {

        fun toJson(bezier: InterpolatingConicSection): String = prettyGson
                .toJson(InterpolatingConicSectionJson(
                        bezier.begin.run { PointJson(x, y, z, r) },
                        bezier.middle.run { PointJson(x, y, z, r) },
                        bezier.end.run { PointJson(x, y, z, r) },
                        bezier.weight))

        fun fromJson(json: String): Option<InterpolatingConicSection> {
            return try {
                prettyGson.fromJson<InterpolatingConicSectionJson>(json).run {
                    Option(InterpolatingConicSection(
                            begin.run { Point.xyzr(x, y, z, r) },
                            middle.run { Point.xyzr(x, y, z, r) },
                            end.run { Point.xyzr(x, y, z, r) },
                            weight))
                }
            } catch (e: Exception) {
                None()
            }
        }
    }
}
