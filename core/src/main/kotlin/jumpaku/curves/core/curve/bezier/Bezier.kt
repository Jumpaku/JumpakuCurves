package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.util.component1
import jumpaku.curves.core.util.component2
import jumpaku.curves.core.util.isOdd
import org.apache.commons.math3.util.CombinatoricsUtils
import org.apache.commons.math3.util.FastMath


class Bezier(controlPoints: Iterable<Point>) : Curve, Differentiable, ToJson {

    constructor(vararg controlPoints: Point): this(controlPoints.asIterable())

    val controlPoints: List<Point> = controlPoints.toList()

    override val domain: Interval get() = Interval.ZERO_ONE

    override val derivative: BezierDerivative get() {
        val cp = controlPoints.map(Point::toCrisp)
        val vs = cp.zip(cp.drop(1)) { pre, post -> (post - pre)*degree.toDouble() }
        return BezierDerivative(vs)
    }

    val degree: Int get() = controlPoints.size - 1

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement =
            jsonObject("controlPoints" to jsonArray(controlPoints.map { it.toJson() }))

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return createEvaluatedPoint(t, controlPoints)
    }

    override fun differentiate(t: Double): Vector = derivative(t)

    override fun toCrisp(): Bezier = Bezier(controlPoints.map { it.toCrisp() })

    fun transform(a: Transform): Bezier = Bezier(controlPoints.map(a::invoke))

    fun restrict(i: Interval): Bezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Bezier {
        require(Interval(begin, end) in domain) { "Interval($domain) is out of domain($domain)" }

        return subdivide(end)._1().subdivide(begin / end)._2()
    }

    fun reverse(): Bezier = Bezier(controlPoints.reversed())

    fun elevate(): Bezier = Bezier(createElevatedControlPoints(controlPoints))

    fun reduce(): Bezier {
        require(degree >= 1) { "degree($degree) is too small" }

        return Bezier(createReducedControlPoints(controlPoints))
    }

    fun subdivide(t: Double): Tuple2<Bezier, Bezier> {
        require(t in domain) { "t($t) is out of domain($domain)" }

        return createSubdividedControlPoints(t, controlPoints).map(::Bezier, ::Bezier)
    }

    fun extend(t: Double): Bezier {
        require(t <= domain.begin || domain.end <= t) { "t($t) is in domain($domain)" }

        return createSubdividedControlPoints(t, controlPoints)
                .let { (a, b) -> Bezier(if(t <= domain.begin) b else a) }
    }

    companion object {

        fun fromJson(json: JsonElement): Bezier = Bezier(json["controlPoints"].array.map { Point.fromJson(it) })

        fun basis(degree: Int, i: Int, t: Double): Double {
            val comb = CombinatoricsUtils::binomialCoefficientDouble
            return comb(degree, i) * FastMath.pow(t, i)*FastMath.pow(1 - t, degree - i)
        }

        fun <P : Lerpable<P>> decasteljau(t: Double, cps: List<P>): List<P> =
                cps.zipWithNext { p0, p1 -> p0.lerp(t, p1) }

        internal tailrec fun <P : Lerpable<P>> createEvaluatedPoint(t: Double, cp: List<P>): P =
                if (cp.size == 1) cp.first() else createEvaluatedPoint(t, decasteljau(t, cp))

        internal fun <P : Lerpable<P>> createElevatedControlPoints(cp: List<P>): List<P> {
            val n = cp.size - 1

            return (0..(n + 1)).map {
                when (it) {
                    0 -> cp.first()
                    n + 1 -> cp.last()
                    else -> cp[it].lerp(it / (n + 1).toDouble(), cp[it - 1])
                }
            }
        }

        internal fun <P : Lerpable<P>> createSubdividedControlPoints(t: Double, cp: List<P>): Tuple2<List<P>, List<P>> {
            var tmp = cp
            val first = mutableListOf(tmp.first())
            val second = mutableListOf(tmp.last())

            while (tmp.size > 1) {
                tmp = decasteljau(t, tmp)
                first.add(tmp.first())
                second.add(0, tmp.last())
            }

            return Tuple2(first, second)
        }

        internal fun <P : Lerpable<P>> createReducedControlPoints(cp: List<P>): List<P>  {
            val m = cp.size
            val n = m - 1
            return when {
                m == 2 -> listOf(cp[0].middle(cp[1]))
                m.isOdd() -> {
                    val r = (m - 3) / 2
                    val first = generateSequence(Tuple2(cp.first(), 1)) {
                        (qi, i) -> Tuple2(cp[i].lerp(i / (i - n).toDouble(), qi), i + 1)
                    }.asIterable()
                            .take(r + 1)
                    val second = generateSequence(Tuple2(cp.last(), n - 2)) {
                        (qi, i) -> Tuple2(cp[i+1].lerp((i + 1 - n)/(i + 1.0), qi), i - 1)
                    }.asIterable()
                            .take(r + 1)
                    (first + second.reversed()).map { it._1() }
                }
                else -> {
                    val r = (m - 2) / 2
                    val first = generateSequence(Tuple2(cp.first(), 1)) {
                        (qi, i) -> Tuple2(cp[i].lerp(i / (i - n).toDouble(), qi), i + 1)
                    }.asIterable()
                            .take(r).map { it._1() }
                    val second = generateSequence(Tuple2(cp.last(), n - 2)) {
                        (qi, i) -> Tuple2(cp[i+1].lerp((i + 1 - n)/(i + 1.0), qi), i - 1)
                    }.asIterable()
                            .take(r).map { it._1() }
                    val pl = cp[r].lerp(r / (r - n).toDouble(), first.last())
                    val pr = cp[r + 1].lerp((r + 1 - n) / (r + 1.0), second.last())
                    (first + listOf(pl.middle(pr)) + second.reversed())

                }
            }
        }
    }
}
