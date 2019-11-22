package jumpaku.curves.core.curve.bezier

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.weighted
import jumpaku.curves.core.transform.Transform
import org.apache.commons.math3.util.CombinatoricsUtils
import org.apache.commons.math3.util.FastMath


class Bezier private constructor(private val rationalBezier: RationalBezier)
    : Curve by rationalBezier, Differentiable, ToJson {

    constructor(controlPoints: Iterable<Point>) : this(RationalBezier(controlPoints.map { it.weighted() }))

    constructor(vararg controlPoints: Point) : this(controlPoints.asIterable())

    val controlPoints: List<Point> get() = rationalBezier.controlPoints

    val degree: Int get() = rationalBezier.degree

    override val derivative: BezierDerivative by lazy {
        val cp = controlPoints.map(Point::toCrisp)
        val vs = cp.zip(cp.drop(1)) { pre, post -> (post - pre) * degree.toDouble() }
        BezierDerivative(vs)
    }

    override fun toCrisp(): Bezier = Bezier(rationalBezier.toCrisp())

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement =
            jsonObject("controlPoints" to jsonArray(controlPoints.map { it.toJson() }))

    fun transform(a: Transform): Bezier = Bezier(rationalBezier.transform(a))

    fun restrict(i: Interval): Bezier = restrict(i.begin, i.end)

    fun restrict(begin: Double, end: Double): Bezier = Bezier(rationalBezier.restrict(begin, end))

    fun reverse(): Bezier = Bezier(rationalBezier.reverse())

    fun elevate(): Bezier = Bezier(rationalBezier.elevate())

    fun reduce(): Bezier = Bezier(rationalBezier.reduce())

    fun subdivide(t: Double): Pair<Bezier, Bezier> = rationalBezier.subdivide(t).run { Pair(Bezier(first), Bezier(second)) }

    fun extend(t: Double): Bezier = Bezier(rationalBezier.extend(t))

    companion object {

        fun fromJson(json: JsonElement): Bezier = Bezier(json["controlPoints"].array.map { Point.fromJson(it) })

        fun basis(degree: Int, i: Int, t: Double): Double {
            val comb = CombinatoricsUtils::binomialCoefficientDouble
            return comb(degree, i) * FastMath.pow(t, i) * FastMath.pow(1 - t, degree - i)
        }

        fun <P : Lerpable<P>> decasteljau(t: Double, cps: List<P>): List<P> =
                cps.zipWithNext { p0, p1 -> p0.lerp(t, p1) }
    }
}
