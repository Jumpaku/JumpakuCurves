package org.jumpaku.curve.bspline

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import org.jumpaku.affine.*
import org.jumpaku.curve.*
import org.jumpaku.curve.bezier.BezierDerivative
import org.jumpaku.json.prettyGson


class BSplineDerivative(val asBSpline: BSpline) : Derivative, Differentiable {

    constructor(controlVectors: Array<Vector>, knots: Array<Knot>) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    override val domain: Interval get() = asBSpline.domain

    override val derivative: Derivative get() = asBSpline.derivative

    val controlVectors: Array<Vector> get() = asBSpline.controlPoints.map(Point::toVector)

    val knots: Array<Knot> get() = asBSpline.knots

    val knotValues: Array<Double> get() = asBSpline.knotValues

    val degree: Int get() = asBSpline.degree

    override fun toString(): String = BSplineDerivativeJson.toJson(this)

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

data class BSplineDerivativeJson(val controlVectors: Array<VectorJson>, val knots: Array<KnotJson>){

    companion object{

        fun toJson(s: BSplineDerivative): String = prettyGson.toJson(BSplineDerivativeJson(
                s.controlVectors.map { VectorJson(it.x, it.y, it.z) },
                s.knots.map { KnotJson(it.value, it.multiplicity) }))

        fun fromJson(json: String): Option<BSplineDerivative>{
            return try {
                Option(prettyGson.fromJson<BSplineDerivativeJson>(json)
                        .run {
                            BSplineDerivative(
                                    controlVectors.map { Vector(it.x, it.y, it.z) },
                                    knots.map { Knot(it.value, it.multiplicity) })
                        })
            }
            catch (e: Exception){
                None()
            }
        }
    }
}