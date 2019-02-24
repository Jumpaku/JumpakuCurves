package jumpaku.core.curve.rationalbezier


import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Derivative
import jumpaku.core.curve.Differentiable
import jumpaku.core.curve.Interval
import jumpaku.core.geom.*
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.util.*
import org.apache.commons.math3.util.FastMath
import kotlin.math.absoluteValue


/**
 * Conic section defined by 3 representation points.
 */
class ConicSection(val begin: Point, val far: Point, val end: Point, val weight: Double)
    : Curve, Differentiable, ToJson {

    val representPoints: List<Point> get() = listOf(begin, far, end)

    val degree = 2

    override val domain: Interval = Interval.ZERO_ONE

    override val derivative: Derivative get() = object : Derivative {
        override val domain: Interval = this@ConicSection.domain
        override fun evaluate(t: Double): Vector = this@ConicSection.differentiate(t)
    }

    fun toCrispQuadratic(): Option<RationalBezier> = optionWhen(1.0.tryDiv(weight).isSuccess) {
        RationalBezier(listOf(
                begin.toCrisp(),
                far.lerp(-1 / weight, begin.middle(end)).toCrisp(),
                end.toCrisp()
        ).zip(listOf(1.0, weight, 1.0), ::WeightedPoint))
    }

    override fun differentiate(t: Double): Vector {
        require(t in domain) { "t($t) is out of domain($domain)" }

        val g = (1 - t)*(1 - 2*t)*begin.toVector() + 2*t*(1 - t)*(1 + weight)*far.toVector() + t*(2*t - 1)*end.toVector()
        val dg_dt = (4*t - 3)*begin.toVector() + 2*(1 - 2*t)*(1 + weight)*far.toVector() + (4*t - 1)*end.toVector()
        val f = RationalBezier.bezier1D(t, listOf(1.0, weight, 1.0))
        val df_dt = 2*(weight - 1)*(1 - 2*t)

        return ((dg_dt*f - g*df_dt)/(f*f)).orThrow()
    }

    override fun evaluate(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        val wt = RationalBezier.bezier1D(t, listOf(1.0, weight, 1.0))
        return far.lerp((1 - t) * (1 - 2 * t) / wt to begin, t * (2 * t - 1) / wt to end)
    }

    fun transform(a: Transform): ConicSection = ConicSection(a(begin), a(far), a(end), weight)

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "begin" to begin.toJson(), "far" to far.toJson(), "end" to end.toJson(), "weight" to weight.toJson())

    override fun toCrisp(): ConicSection = ConicSection(begin.toCrisp(), far.toCrisp(), end.toCrisp(), weight)

    fun reverse(): ConicSection = ConicSection(end, far, begin, weight)

    fun complement(): ConicSection {
        val farComplement = 1.0.tryDiv(1 - weight)
                .tryMap { far.lerp(it to begin, it to end) }
                .tryRecover { far }.orThrow()
        return ConicSection(begin, farComplement, end, -weight)
    }

    fun center(): Option<Point> = 0.5.tryDiv(1 - weight).tryMap { far.lerp(it to begin, it to end) }.value()

    /**
     * Subdivides this at t into 2 conic sections
     */
    fun subdivide(t: Double): Tuple2<ConicSection, ConicSection> {
        val w = weight
        val p0 = begin.toVector()
        val p1 = far.toVector()
        val p2 = end.toVector()
        val m = begin.middle(end)
        val rootwt = FastMath.sqrt(RationalBezier.bezier1D(t, listOf(1.0, w, 1.0)))

        val begin0 = begin
        val end0 = evaluate(t)
        val weight0 = (1 - t + t*w)/rootwt
        val far0P = ((begin0.toVector() + end0.toVector()) * rootwt *0.5 + (1 - t) * p0 + t * ((1 + w) * p1 - m.toVector())) / (rootwt + 1 - t + t * w)
        val far0R = FastMath.abs(0.5*(2 - 3*t + rootwt*(2*t*t - 3*t + 2))/(rootwt + 1 - t + t*w))*begin.r +
                FastMath.abs((t*(1 + w)*(1 + (1 - t)/rootwt))/(rootwt + 1 - t + t*w))*far.r +
                FastMath.abs(0.5*(-t + t*(2*t - 1)/rootwt)/(rootwt + 1 - t + t*w))*end.r
        val far0 = Point(far0P.orThrow(), far0R)

        val begin1 = end0
        val end1 = end
        val weight1 = ((1 - t)*w + t)/rootwt
        val far1P = ((begin1.toVector() + end1.toVector()) * rootwt *0.5 + (1 - t) * ((1 + w) * p1 - m.toVector()) + t * p2) / (rootwt + (1 - t) * w + t)
        val far1R = FastMath.abs(0.5*(3*t - 1 + rootwt*(2*t*t -t + 1))/(rootwt + (1 - t)*w + t))*begin.r +
                FastMath.abs(((1 - t)*(1 + w)*(1 + t/rootwt))/(rootwt + (1 - t)*w + t))*far.r +
                FastMath.abs(0.5*((1 - t)*((1 - 2*t)/rootwt - 1))/(rootwt + (1 - t)*w + t))*end.r
        val far1 = Point(far1P.orThrow(), far1R)

        return Tuple2(ConicSection(begin0, far0, end0, weight0), ConicSection(begin1, far1, end1, weight1))
    }

    fun restrict(interval: Interval): ConicSection = restrict(interval.begin, interval.end)

    fun restrict(begin: Double, end: Double): ConicSection {
        val t = begin/end
        val a = FastMath.sqrt(RationalBezier.bezier1D(end, listOf(1.0, weight, 1.0)))
        return subdivide(end)._1().subdivide(a*t/(t*(a - 1) + 1))._2()
    }

    companion object {

        /**
         * Calculates a weight as (l^2 - h^2)/(l^2 + h^2),
         * where h is distance between far and line(begin, end), l = |begin - end|/2.
         *  an elliptic arc with this weight is a sheared circular arc which has the same weight.
         */
        fun shearedCircularArc(begin: Point, far: Point, end: Point): ConicSection {
            val hh = line(begin, end).tryMap { far.distSquare(it) }.value().orDefault { begin.distSquare(far) }
            val ll = (begin - end).square()/4
            return ConicSection(begin, far, end, ((ll - hh) / (ll + hh)).coerceIn(-0.999, 0.999))
        }

        fun lineSegment(begin: Point, end: Point): ConicSection = ConicSection(begin, begin.middle(end), end, 1.0)

        fun fromJson(json: JsonElement): ConicSection =
            ConicSection(
                    Point.fromJson(json["begin"]),
                    Point.fromJson(json["far"]),
                    Point.fromJson(json["end"]),
                    json["weight"].double)
    }
}
