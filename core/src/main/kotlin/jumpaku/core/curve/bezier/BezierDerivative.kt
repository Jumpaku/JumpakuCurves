package org.jumpaku.core.curve.bezier

import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import org.jumpaku.core.curve.Derivative
import org.jumpaku.core.curve.Differentiable
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.json.prettyGson


class BezierDerivative(private val bezier: Bezier) : Derivative, Differentiable {

    override val derivative: BezierDerivative get() = toBezier().derivative

    override val domain: Interval get() = toBezier().domain

    val controlVectors: Array<Vector> get() = toBezier().controlPoints.map(Point::toVector)

    val degree: Int get() = toBezier().degree

    constructor(controlVectors: Array<Vector>): this(Bezier(controlVectors.map { Point(it) }))

    constructor(controlVectors: Iterable<Vector>): this(Array.ofAll(controlVectors))

    constructor(vararg controlVectors: Vector): this(controlVectors.asIterable())

    fun toBezier(): Bezier = Bezier(bezier.controlPoints.map { it.toCrisp() })

    override fun evaluate(t: Double): Vector = toBezier()(t).toVector()

    override fun differentiate(t: Double): Vector = toBezier().differentiate(t)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): BezierDerivativeJson = BezierDerivativeJson(this)

    fun restrict(i: Interval): BezierDerivative = BezierDerivative(toBezier().restrict(i))

    fun restrict(begin: Double, end: Double): BezierDerivative = BezierDerivative(toBezier().restrict(begin, end))

    fun reverse(): BezierDerivative = BezierDerivative(toBezier().reverse())

    fun elevate(): BezierDerivative = BezierDerivative(toBezier().elevate())

    fun reduce(): BezierDerivative = BezierDerivative(toBezier().reduce())

    fun subdivide(t: Double): Tuple2<BezierDerivative, BezierDerivative> = toBezier()
            .subdivide(t).map(::BezierDerivative, ::BezierDerivative)

    fun extend(t: Double): BezierDerivative = BezierDerivative(toBezier().extend(t))
}

data class BezierDerivativeJson(private val controlVectors: List<Vector>){

    constructor(bezierDerivative: BezierDerivative) : this(bezierDerivative.controlVectors.toJavaList())

    fun bezierDerivative(): BezierDerivative = BezierDerivative(controlVectors)
}