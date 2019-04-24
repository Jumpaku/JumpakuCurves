package jumpaku.curves.core.curve.bspline

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.BezierDerivative
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector


class BSplineDerivative(private val bSpline: BSpline) : Derivative, Differentiable, ToJson {

    constructor(controlVectors: Iterable<Vector>, knots: KnotVector) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    fun toBSpline(): BSpline = bSpline.toCrisp()

    override val domain: Interval get() = toBSpline().domain

    override val derivative: BSplineDerivative get() = toBSpline().derivative

    val controlVectors: List<Vector> get() = toBSpline().controlPoints.map(Point::toVector)

    val knotVector: KnotVector get() = toBSpline().knotVector

    val degree: Int get() = toBSpline().degree

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlVectors" to jsonArray(controlVectors.map { it.toJson() }),
            "degree" to degree,
            "knots" to jsonArray(knotVector.knots.map { it.toJson() }))

    override fun evaluate(t: Double): Vector = toBSpline()(t).toVector()

    override fun differentiate(t: Double): Vector = toBSpline().derivative.evaluate(t)

    fun restrict(begin: Double, end: Double): BSplineDerivative = BSplineDerivative(toBSpline().restrict(begin, end))

    fun restrict(i: Interval): BSplineDerivative = BSplineDerivative(toBSpline().restrict(i))

    fun reverse(): BSplineDerivative = BSplineDerivative(toBSpline().reverse())

    fun clamp(): BSplineDerivative = BSplineDerivative(toBSpline().clamp())

    fun close(): BSplineDerivative = BSplineDerivative(toBSpline().close())

    fun insertKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(toBSpline().insertKnot(t, m))

    fun removeKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(toBSpline().removeKnot(t, m))

    fun toBeziers(): List<BezierDerivative> = toBSpline().toBeziers().map(::BezierDerivative)

    fun subdivide(t: Double): Pair<Option<BSplineDerivative>, Option<BSplineDerivative>> = toBSpline().subdivide(t)
            .run { Pair(first.map { BSplineDerivative(it) }, second.map { BSplineDerivative(it) }) }

    companion object {

        fun fromJson(json: JsonElement): BSplineDerivative {
            val d = json["degree"].int
            val cv = json["controlVectors"].array.map { Vector.fromJson(it) }
            val ks = json["knots"].array.map { Knot.fromJson(it) }
            return BSplineDerivative(cv, KnotVector(d, ks))
        }
    }
}
