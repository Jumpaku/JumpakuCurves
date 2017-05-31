package org.jumpaku.curve.bezier

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import org.jumpaku.affine.Crisp
import org.jumpaku.affine.Point
import org.jumpaku.affine.Vector
import org.jumpaku.affine.VectorJson
import org.jumpaku.curve.Derivative
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.Interval
import org.jumpaku.json.prettyGson


class BezierDerivative(val asBezier: Bezier) : Derivative, Differentiable {

    override val derivative: BezierDerivative get() = asBezier.derivative

    override val domain: Interval get() = asBezier.domain

    val controlVectors: Array<Vector> get() = asBezier.controlPoints.map(Point::toVector)

    val degree: Int get() = asBezier.degree

    constructor(controlVectors: Array<Vector>): this(Bezier(controlVectors.map(::Crisp)))

    constructor(controlVectors: Iterable<Vector>): this(Array.ofAll(controlVectors))

    constructor(vararg controlVectors: Vector): this(controlVectors.asIterable())

    override fun evaluate(t: Double): Vector = asBezier.evaluate(t).toVector()

    override fun differentiate(t: Double): Vector = asBezier.differentiate(t)

    override fun toString(): String = BezierDerivativeJson.toJson(this)

    fun restrict(i: Interval): BezierDerivative = BezierDerivative(asBezier.restrict(i))

    fun restrict(begin: Double, end: Double): BezierDerivative = BezierDerivative(asBezier.restrict(begin, end))

    fun reverse(): BezierDerivative = BezierDerivative(asBezier.reverse())

    fun elevate(): BezierDerivative = BezierDerivative(asBezier.elevate())

    fun reduce(): BezierDerivative = BezierDerivative(asBezier.reduce())

    fun subdivide(t: Double): Tuple2<BezierDerivative, BezierDerivative> = asBezier.subdivide(t)
            .map<BezierDerivative, BezierDerivative>(::BezierDerivative, ::BezierDerivative)
}

class BezierDerivativeJson(controlVectors: Array<Vector>){

    private val controlVectors: kotlin.Array<VectorJson> = controlVectors.map { VectorJson(it.x, it.y, it.z) }
            .toJavaArray(VectorJson::class.java)

    companion object {

        fun toJson(derivative: BezierDerivative): String = prettyGson.toJson(BezierDerivativeJson(
                derivative.controlVectors))

        fun fromJson(json: String): Option<BezierDerivative> {
            return try {
                Option(BezierDerivative(prettyGson.fromJson<BezierDerivativeJson>(json)
                        .controlVectors.map(VectorJson::vector)))
            } catch(e: Exception) {
                None()
            }
        }
    }
}