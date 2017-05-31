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

    constructor(controlVectors: Array<Vector>, knots: Array<Knot>) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    override val domain: Interval get() = asBSpline.domain

    override val derivative: BSplineDerivative get() = asBSpline.derivative

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

class BSplineDerivativeJson(controlVectors: Array<Vector>, knots: Array<Knot>){

    private val controlVectors: kotlin.Array<VectorJson> = controlVectors.map { VectorJson(it.x, it.y, it.z) }
            .toJavaArray(VectorJson::class.java)

    private val knots: kotlin.Array<KnotJson> = knots.map { KnotJson(it.value, it.multiplicity) }
            .toJavaArray(KnotJson::class.java)

    fun bSplineDerivative(): BSplineDerivative = BSplineDerivative(
            Array(*controlVectors).map(VectorJson::vector), Array(*knots).map(KnotJson::knot))

    companion object{

        fun toJson(s: BSplineDerivative): String = prettyGson.toJson(BSplineDerivativeJson(s.controlVectors, s.knots))

        fun fromJson(json: String): Option<BSplineDerivative>{
            return try {
                Option(prettyGson.fromJson<BSplineDerivativeJson>(json).bSplineDerivative())
            }
            catch (e: Exception){
                None()
            }
        }
    }
}