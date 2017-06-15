package org.jumpaku.core.curve.bspline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import org.jumpaku.core.affine.*
import org.jumpaku.core.curve.*
import org.jumpaku.core.curve.bezier.BezierDerivative
import org.jumpaku.core.json.prettyGson


class BSplineDerivative(val asBSpline: BSpline) : Derivative, Differentiable {

    constructor(controlVectors: Iterable<Vector>, knots: Iterable<Knot>) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    override val domain: Interval get() = asBSpline.domain

    override val derivative: BSplineDerivative get() = asBSpline.derivative

    val controlVectors: Array<Vector> get() = asBSpline.controlPoints.map(Point::toVector)

    val knots: Array<Knot> get() = asBSpline.knots

    val knotValues: Array<Double> get() = asBSpline.knotValues

    val degree: Int get() = asBSpline.degree

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): BSplineDerivativeJson = BSplineDerivativeJson(this)

    override fun evaluate(t: Double): Vector = asBSpline.evaluate(t).toVector()

    override fun differentiate(t: Double): Vector = asBSpline.derivative.evaluate(t)

    fun restrict(begin: Double, end: Double): BSplineDerivative {
        return BSplineDerivative(asBSpline.restrict(begin, end))
    }

    fun restrict(i: Interval): BSplineDerivative = BSplineDerivative(asBSpline.restrict(i))

    fun reverse(): BSplineDerivative = BSplineDerivative(asBSpline.reverse())

    fun insertKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(asBSpline.insertKnot(t, m))

    fun insertKnot(i: Int, m: Int = 1): BSplineDerivative = BSplineDerivative(asBSpline.insertKnot(i, m))

    fun toBeziers(): Array<BezierDerivative> = asBSpline.toBeziers().map(::BezierDerivative)

    fun subdivide(t: Double): Tuple2<BSplineDerivative, BSplineDerivative> {
        return asBSpline.subdivide(t).map<BSplineDerivative, BSplineDerivative>(
                ::BSplineDerivative, ::BSplineDerivative)
    }
}

data class BSplineDerivativeJson(private val controlVectors: List<VectorJson>, private val knots: List<KnotJson>){

    constructor(bSplineDerivative: BSplineDerivative) : this(
            bSplineDerivative.controlVectors.map(Vector::json).toJavaList(),
            bSplineDerivative.knots.map(Knot::json).toJavaList())

    fun bSplineDerivative(): BSplineDerivative = BSplineDerivative(controlVectors.map(VectorJson::vector), knots.map(KnotJson::knot))
}