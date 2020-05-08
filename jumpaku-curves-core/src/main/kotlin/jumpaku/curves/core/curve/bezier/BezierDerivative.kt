package jumpaku.curves.core.curve.bezier


import jumpaku.curves.core.curve.Derivative
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector


class BezierDerivative(private val bezier: Bezier) : Derivative, Differentiable {

    constructor(controlVectors: Iterable<Vector>) : this(Bezier(controlVectors.map { Point(it) }))

    constructor(vararg controlVectors: Vector) : this(controlVectors.asIterable())

    override val derivative: BezierDerivative get() = toBezier().derivative

    override val domain: Interval get() = toBezier().domain

    val controlVectors: List<Vector> get() = toBezier().controlPoints.map(Point::toVector)

    val degree: Int get() = toBezier().degree

    fun toBezier(): Bezier = Bezier(bezier.controlPoints.map { it.toCrisp() })

    override fun evaluate(t: Double): Vector = toBezier()(t).toVector()

    override fun differentiate(t: Double): Vector = toBezier().differentiate(t)

    override fun toString(): String = "BezierDerivative(controlVectors=$controlVectors)"

    fun restrict(i: Interval): BezierDerivative = BezierDerivative(toBezier().restrict(i))

    fun restrict(begin: Double, end: Double): BezierDerivative = BezierDerivative(toBezier().restrict(begin, end))

    fun reverse(): BezierDerivative = BezierDerivative(toBezier().reverse())

    fun elevate(): BezierDerivative = BezierDerivative(toBezier().elevate())

    fun reduce(): BezierDerivative = BezierDerivative(toBezier().reduce())

    fun subdivide(t: Double): Pair<BezierDerivative, BezierDerivative> = toBezier()
            .subdivide(t).run { Pair(BezierDerivative(first), BezierDerivative(second)) }

    fun extend(t: Double): BezierDerivative = BezierDerivative(toBezier().extend(t))

}

