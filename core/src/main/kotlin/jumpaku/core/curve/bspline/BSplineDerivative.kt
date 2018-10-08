package jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple2
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.curve.*
import jumpaku.core.curve.bezier.BezierDerivative
import jumpaku.core.json.ToJson
import jumpaku.core.util.Option
import jumpaku.core.util.Result
import jumpaku.core.util.result


class BSplineDerivative(private val bSpline: BSpline) : Derivative, Differentiable, ToJson {

    constructor(controlVectors: Iterable<Vector>, knots: KnotVector) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    fun toBSpline(): BSpline = BSpline(bSpline.controlPoints.map { it.toCrisp() }, bSpline.knotVector)

    override val domain: Interval get() = toBSpline().domain

    override val derivative: BSplineDerivative get() = toBSpline().derivative

    val controlVectors: List<Vector> get() = toBSpline().controlPoints.map(Point::toVector)

    val knotVector: KnotVector get() = toBSpline().knotVector

    val degree: Int get() = toBSpline().degree

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "controlVectors" to jsonArray(controlVectors.map { it.toJson() }),
            "knotVector" to knotVector.toJson())

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

    fun subdivide(t: Double): Tuple2<Option<BSplineDerivative>, Option<BSplineDerivative>> =
            toBSpline().subdivide(t).map({ it.map { BSplineDerivative(it) } }, { it.map { BSplineDerivative(it) } })

    companion object {

        fun fromJson(json: JsonElement): Result<BSplineDerivative> = result {
            BSplineDerivative(
                    json["controlVectors"].array.flatMap { Vector.fromJson(it).value() },
                    KnotVector.fromJson(json["knotVector"]).orThrow())
        }
    }
}
