package org.jumpaku.curve.bezier

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import io.vavr.API.List
import io.vavr.API.Stream
import io.vavr.API.Tuple
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Stream
import org.jumpaku.util.*
import org.jumpaku.affine.Fuzzy
import org.jumpaku.affine.Point
import org.jumpaku.affine.Vector
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.FuzzyCurve
import org.jumpaku.curve.Interval
import org.jumpaku.curve.polyline.Polyline
import org.jumpaku.json.prettyGson


class Bezier(val controlPoints: Array<Point>) : FuzzyCurve, Differentiable{

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: BezierDerivative by lazy {
        val cp = controlPoints.map(Point::toCrisp)
        val vs = cp.zipWith(cp.tail(), { pre, post -> (post - pre)*degree.toDouble() })
        BezierDerivative(vs)
    }

    val degree: Int = controlPoints.size() - 1

    constructor(controlPoints: Iterable<Point>): this(Array.ofAll(controlPoints))

    constructor(vararg controlPoints: Point): this(Stream(*controlPoints))

    override fun toString(): String = toJson(this)

    override fun evaluate(t: Double): Point {
        if (t !in domain) {
            throw IllegalArgumentException("t must be in $domain, but t = $t")
        }

        var cps = controlPoints
        while (cps.size() > 1) {
            cps = decasteljau(t, cps)
        }

        return cps.head()
    }

    override fun differentiate(t: Double): Vector = derivative(t)

    override fun sampleArcLength(n: Int): Array<Point> = Polyline.approximate(this).sampleArcLength(n)

    fun restrict(i: Interval): Bezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Bezier {
        if (Interval(begin, end) !in domain) {
            throw IllegalArgumentException("Interval i must be a subset of this domain")
        }

        return subdivide(end)._1().subdivide(begin / end)._2()
    }

    fun reverse(): Bezier = Bezier(controlPoints.reverse())

    fun elevate(): Bezier = Bezier(createElevatedControlPoints())

    private fun createElevatedControlPoints(): Array<Point> {
        val n = degree
        val cp = controlPoints

        return Stream.rangeClosed(0, n + 1)
                .map {
                    when(it) {
                        0 -> cp.head()
                        n + 1 -> cp.last()
                        else -> cp[it].divide(it / (n + 1).toDouble(), cp[it - 1])
                    }
                }
                .toArray()
    }

    fun reduce(): Bezier {
        if (degree < 1) {
            throw IllegalStateException("degree is too small")
        }

        return Bezier(createReducedControlPoints())
    }

    private fun createReducedControlPoints(): Array<Point> {
        val n = controlPoints.size() - 1
        val m = n + 1

        val cp = controlPoints

        if (m == 2) {
            return Array.of(cp[0].divide(0.5, cp[1]))
        } else if (m % 2 == 0) {
            val r = (m - 2) / 2

            val first = Stream.iterate(Tuple(cp.head(), 0),
                    { (qi, i) -> Tuple(cp[i].divide(1 - 1 / (1.0 - i / n), qi), i + 1) })
                    .take(r).map { it._1() }
                    .toArray()

            val second = Stream.iterate(Tuple(cp.last(), n - 1),
                    { (qi, i) -> Tuple(cp[i].divide(1 - 1.0 / i / n, qi), i - 1) })
                    .take(r).map { it._1() }
                    .reverse().toArray()

            val al = r / (m - 1.0)
            val pl = cp[r].divide(-al / (1.0 - al), first.last())
            val ar = (r + 1) / (m - 1.0)
            val pr = cp[r + 1].divide((-1.0 + ar) / ar, second.head())
            val middle = Stream.of(pl.divide(0.5, pr))

            return Stream.concat(first, middle, second).toArray()
        } else {
            val r = (m - 3) / 2

            return Stream.concat(
                    Stream.iterate(Tuple(cp.head(), 0),
                            { (qi, i) -> Tuple(cp[i].divide(1 - 1 / (1.0 - i / n), qi), i + 1) })
                            .take(r + 1),
                    Stream.iterate(Tuple(cp.last(), n - 1),
                            { (qi, i) -> Tuple(cp[i].divide(1 - 1.0 / i / n, qi), i - 1) })
                            .take(r + 1)
                            .reverse())
                    .map { it._1() }
                    .toArray()
        }
    }

    fun subdivide(t: Double): Tuple2<Bezier, Bezier> {
        if (t !in domain) {
            throw IllegalArgumentException("t($t) is out of domain($domain)")
        }

        return createDividedControlPointsArray(t).map(::Bezier, ::Bezier)
    }

    private fun createDividedControlPointsArray(t: Double): Tuple2<Array<Point>, Array<Point>> {
        var cp = controlPoints
        var first = List(cp.head())
        var second = List(cp.last())

        while (cp.size() > 1) {
            cp = decasteljau(t, cp)
            first = first.prepend(cp.head())
            second = second.prepend(cp.last())
        }

        return Tuple(first.reverse().toArray(), second.toArray())
    }

    companion object {

        fun decasteljau(t: Double, cps: Array<Point>): Array<Point> {
            return cps.zipWith(cps.tail()) { p0, p1 -> p0.divide(t, p1) }
        }

        data class JsonBezier(val controlPoints: kotlin.Array<Point.Companion.JsonPoint>)

        fun toJson(bezier: Bezier): String = prettyGson.toJson(JsonBezier(
                bezier.controlPoints.map { Point.Companion.JsonPoint(it.r, it.x, it.y, it.z) }
                        .toJavaArray(Point.Companion.JsonPoint::class.java)))

        fun fromJson(json: String): Bezier?{
            return try {
                val tmp = prettyGson.fromJson<JsonBezier>(json)
                Bezier(tmp.controlPoints.map { Fuzzy(it.r, it.x, it.y, it.z) })
            }catch(e: Exception){
                when(e){
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}