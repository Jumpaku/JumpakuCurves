package jumpaku.core.curve.rationalbezier


import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.*
import jumpaku.core.affine.transform.Transform
import jumpaku.core.curve.*
import jumpaku.core.curve.arclength.ArcLengthReparametrized
import jumpaku.core.curve.arclength.approximate
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.ToJson
import jumpaku.core.util.*
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import kotlin.math.absoluteValue


/**
 * Conic section defined by 3 representation points.
 */
class ConicSection(
        val begin: Point, val far: Point, val end: Point, val weight: Double)
    : FuzzyCurve, Differentiable, ToJson {

    val representPoints: Array<Point> get() = Array(begin, far, end)

    val degree = 2

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: Derivative
        get() = object : Derivative {
            override val domain: Interval = this@ConicSection.domain
            override fun evaluate(t: Double): Vector = this@ConicSection.differentiate(t)
        }

    fun toCrispQuadratic(): Option<RationalBezier> = Option.`when`(1.0.divOption(weight).isDefined) {
        RationalBezier(Stream(
                begin.toCrisp(),
                far.divide(-1 / weight, begin.middle(end)).toCrisp(),
                end.toCrisp()
        ).zipWith(Stream(1.0, weight, 1.0), ::WeightedPoint))
    }

    override fun differentiate(t: Double): Vector {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val g = (1 - t)*(1 - 2*t)*begin.toVector() + 2*t*(1 - t)*(1 + weight)*far.toVector() + t*(2*t - 1)*end.toVector()
        val dg_dt = (4*t - 3)*begin.toVector() + 2*(1 - 2*t)*(1 + weight)*far.toVector() + (4*t - 1)*end.toVector()
        val f = RationalBezier.bezier1D(t, Array.of(1.0, weight, 1.0))
        val df_dt = 2*(weight - 1)*(1 - 2*t)

        return (dg_dt*f - g*df_dt)/(f*f)
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val wt = RationalBezier.bezier1D(t, Array(1.0, weight, 1.0))
        val (p0, p1, p2) = representPoints.map { it.toVector() }
        val p = ((1 - t) * (1 - 2 * t) * p0 + 2 * t * (1 - t) * (1 + weight) * p1 + t * (2 * t - 1) * p2) / wt
        val (r0, r1, r2) = representPoints.map { it.r }
        val r = listOf(
                r0 * (1 - t) * (1 - 2 * t) / wt,
                r1 * 2 * (weight + 1) * t * (1 - t) / wt,
                r2 * t * (2 * t - 1) / wt
        ).map { it.absoluteValue }.sum()

        return Point(p, r)
    }

    fun transform(a: Transform): ConicSection = ConicSection(
            a(begin), a(far), a(end), weight)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "begin" to begin.toJson(), "far" to far.toJson(), "end" to end.toJson(), "weight" to weight.toJson())

    override fun toCrisp(): ConicSection = ConicSection(begin.toCrisp(), far.toCrisp(), end.toCrisp(), weight)

    fun reverse(): ConicSection = ConicSection(end, far, begin, weight)

    fun complement(): ConicSection = ConicSection(begin, center().map { it.divide(-1.0, far) }.getOrElse { far }, end, -weight)

    fun center(): Option<Point> = weight.divOption(weight - 1).map { begin.middle(end).divide(it, far) }

    fun subdivide(t: Double): Tuple2<ConicSection, ConicSection> {
        val w = weight
        val p0 = begin.toVector()
        val p1 = far.toVector()
        val p2 = end.toVector()
        val m = begin.middle(end)
        val rootwt = FastMath.sqrt(RationalBezier.bezier1D(t, Array.of(1.0, w, 1.0)))

        val begin0 = begin
        val end0 = evaluate(t)
        val weight0 = (1 - t + t*w)/rootwt
        val far0R = FastMath.abs(0.5*(2 - 3*t + rootwt*(2*t*t - 3*t + 2))/(rootwt + 1 - t + t*w))*begin.r +
                FastMath.abs((t*(1 + w)*(1 + (1 - t)/rootwt))/(rootwt + 1 - t + t*w))*far.r +
                FastMath.abs(0.5*(-t + t*(2*t - 1)/rootwt)/(rootwt + 1 - t + t*w))*end.r
        val far0 = Point(((begin0.toVector() + end0.toVector()) * rootwt / 2.0 + (1 - t) * p0 + t * ((1 + w) * p1 - m.toVector())) / (rootwt + 1 - t + t * w), far0R)

        val begin1 = end0
        val end1 = end
        val weight1 = ((1 - t)*w + t)/rootwt
        val far1R = FastMath.abs(0.5*(3*t - 1 + rootwt*(2*t*t -t + 1))/(rootwt + (1 - t)*w + t))*begin.r +
                FastMath.abs(((1 - t)*(1 + w)*(1 + t/rootwt))/(rootwt + (1 - t)*w + t))*far.r +
                FastMath.abs(0.5*((1 - t)*((1 - 2*t)/rootwt - 1))/(rootwt + (1 - t)*w + t))*end.r
        val far1 = Point(((begin1.toVector() + end1.toVector()) * rootwt / 2.0 + (1 - t) * ((1 + w) * p1 - m.toVector()) + t * p2) / (rootwt + (1 - t) * w + t), far1R)

        return Tuple(ConicSection(begin0, far0, end0, weight0), ConicSection(begin1, far1, end1, weight1))
    }

    fun restrict(interval: Interval): ConicSection = restrict(interval.begin, interval.end)

    fun restrict(begin: Double, end: Double): ConicSection {
        val t = begin/end
        val a = FastMath.sqrt(RationalBezier.bezier1D(end, Array.of(1.0, weight, 1.0)))
        return subdivide(end)._1().subdivide(a*t/(t*(a - 1) + 1))._2()
    }

    override val reparametrized: ArcLengthReparametrized by lazy {
        approximate(this,
                {
                    val cp = (it as ConicSection).representPoints
                    val l0 = Polyline(cp).reparametrizeArcLength().arcLength()
                    val l1 = cp.run { head().dist(last()) }
                    !(Precision.equals(l0, l1, 1.0 / 512) && listOf(1.0, it.weight, 1.0).all { it > 0 })
                },
                { b, i: Interval -> (b as ConicSection).restrict(i) })
    }

    companion object {

        /**
         * Calculates a weight as (l^2 - h^2)/(l^2 + h^2),
         * where h is distance between far and line(begin, end), l = |begin - end|/2.
         *  an elliptic arc with this weight is a sheared circular arc which has the same weight.
         */
        fun shearedCircularArc(begin: Point, far: Point, end: Point): ConicSection {
            val hh = line(begin, end).map { far.distSquare(it) }
                    .getOrElse { begin.distSquare(far) }
            val ll = (begin - end).square()/4
            return ConicSection(begin, far, end, ((ll - hh) / (ll + hh)).coerceIn(-0.999, 0.999))
        }

        fun lineSegment(begin: Point, end: Point): ConicSection = ConicSection(begin, begin.middle(end), end, 1.0)

        fun fromJson(json: JsonElement): Option<ConicSection> = Try.ofSupplier {
            ConicSection(Point.fromJson(json["begin"]).get(), Point.fromJson(json["far"]).get(), Point.fromJson(json["end"]).get(), json["weight"].double)
        }.toOption()
    }
}
