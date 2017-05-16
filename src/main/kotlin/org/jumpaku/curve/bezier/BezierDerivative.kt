package org.jumpaku.curve.bezier

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import io.vavr.Tuple2
import io.vavr.collection.Array
import org.jumpaku.affine.Crisp
import org.jumpaku.affine.Point
import org.jumpaku.affine.Vector
import org.jumpaku.curve.Derivative
import org.jumpaku.curve.Differentiable
import org.jumpaku.curve.Interval
import org.jumpaku.json.prettyGson


class BezierDerivative(val asBezier: Bezier) : Derivative, Differentiable {

    override val derivative: BezierDerivative by lazy { asBezier.derivative }

    override val domain: Interval = asBezier.domain

    val controlVectors: Array<Vector> = asBezier.controlPoints.map(Point::toVector)

    val degree: Int = asBezier.degree

    constructor(controlVectors: Array<Vector>): this(Bezier(controlVectors.map(::Crisp)))

    constructor(controlVectors: Iterable<Vector>): this(Array.ofAll(controlVectors))

    constructor(vararg controlVectors: Vector): this(controlVectors.asIterable())

    override fun evaluate(t: Double): Vector = asBezier.evaluate(t).toVector()

    override fun differentiate(t: Double): Vector = asBezier.differentiate(t)

    override fun toString(): String = toJson(this)

    fun restrict(i: Interval): BezierDerivative = BezierDerivative(asBezier.restrict(i))

    fun restrict(begin: Double, end: Double): BezierDerivative = BezierDerivative(asBezier.restrict(begin, end))

    fun reverse(): BezierDerivative = BezierDerivative(asBezier.reverse())

    fun elevate(): BezierDerivative = BezierDerivative(asBezier.elevate())

    fun reduce(): BezierDerivative = BezierDerivative(asBezier.reduce())

    fun subdivide(t: Double): Tuple2<BezierDerivative, BezierDerivative> = asBezier.subdivide(t)
            .map<BezierDerivative, BezierDerivative>(::BezierDerivative, ::BezierDerivative)

    companion object {

        data class JsonBezierDerivative(val controlVectors: kotlin.Array<Vector>)

        fun toJson(derivative: BezierDerivative): String = prettyGson.toJson(JsonBezierDerivative(
                derivative.controlVectors.toJavaArray(Vector::class.java)))

        fun fromJson(json: String): BezierDerivative?{
            return try {
                val tmp = prettyGson.fromJson<JsonBezierDerivative>(json)
                BezierDerivative(tmp.controlVectors.map { Vector(it.x, it.y, it.z) })
            }catch(e: Exception){
                when(e){
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}