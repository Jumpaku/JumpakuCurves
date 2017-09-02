package org.jumpaku.core.curve.bspline

import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.VectorJson
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.bezier.BezierDerivative
import org.jumpaku.core.json.prettyGson


class BSplineDerivative(private val bSpline: BSpline) : Derivative, Differentiable {

    constructor(controlVectors: Iterable<Vector>, knots: KnotVector) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    fun toBSpline(): BSpline = BSpline(bSpline.controlPoints.map { it.toCrisp() }, bSpline.knotVector)

    override val domain: Interval get() = toBSpline().domain

    override val derivative: BSplineDerivative get() = toBSpline().derivative

    val controlVectors: Array<Vector> get() = toBSpline().controlPoints.map(Point::toVector)

    val knotVector: KnotVector get() = toBSpline().knotVector

    val degree: Int get() = toBSpline().degree

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): BSplineDerivativeJson = BSplineDerivativeJson(this)

    override fun evaluate(t: Double): Vector = toBSpline()(t).toVector()

    override fun differentiate(t: Double): Vector = toBSpline().derivative.evaluate(t)

    fun restrict(begin: Double, end: Double): BSplineDerivative = BSplineDerivative(toBSpline().restrict(begin, end))

    fun restrict(i: Interval): BSplineDerivative = BSplineDerivative(toBSpline().restrict(i))

    fun reverse(): BSplineDerivative = BSplineDerivative(toBSpline().reverse())

    fun clamp(): BSplineDerivative = BSplineDerivative(toBSpline().clamp())

    fun close(): BSplineDerivative = BSplineDerivative(toBSpline().close())

    fun insertKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(toBSpline().insertKnot(t, m))

    fun toBeziers(): Array<BezierDerivative> = toBSpline().toBeziers().map(::BezierDerivative)

    fun subdivide(t: Double): Tuple2<BSplineDerivative, BSplineDerivative> {
        return toBSpline().subdivide(t).map(::BSplineDerivative, ::BSplineDerivative)
    }
}

data class BSplineDerivativeJson(val controlVectors: List<VectorJson>, val knotVector: KnotVectorJson){

    constructor(bSplineDerivative: BSplineDerivative) : this(
            bSplineDerivative.controlVectors.map(Vector::json).toJavaList(),
            bSplineDerivative.knotVector.json())

    fun bSplineDerivative(): BSplineDerivative = BSplineDerivative(controlVectors.map(VectorJson::vector), knotVector.knotVector())
}