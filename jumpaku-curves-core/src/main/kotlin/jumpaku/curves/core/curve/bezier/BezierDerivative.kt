package jumpaku.curves.core.curve.bezier


import jumpaku.curves.core.curve.Derivative
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector


class BezierDerivative(bezier: Bezier) : Derivative, Differentiable {

    constructor(controlVectors: Iterable<Vector>) : this(Bezier(controlVectors.map { Point(it) }))

    constructor(vararg controlVectors: Vector) : this(controlVectors.asIterable())

    val curve: Bezier = bezier.toCrisp()

    override val domain: Interval = curve.domain

    val controlVectors: List<Vector> = curve.controlPoints.map(Point::toVector)

    val degree: Int = curve.degree

    override fun invoke(t: Double): Vector = curve(t).toVector()

    override fun differentiate(): BezierDerivative = curve.differentiate()

    override fun toString(): String = "BezierDerivative(controlVectors=$controlVectors)"

    fun restrict(i: Interval): BezierDerivative = BezierDerivative(curve.restrict(i))

    fun restrict(begin: Double, end: Double): BezierDerivative = BezierDerivative(curve.restrict(begin, end))

    fun reverse(): BezierDerivative = BezierDerivative(curve.reverse())

    fun elevate(): BezierDerivative = BezierDerivative(curve.elevate())

    fun reduce(): BezierDerivative = BezierDerivative(curve.reduce())

    fun subdivide(t: Double): Pair<BezierDerivative, BezierDerivative> = curve
            .subdivide(t).run { Pair(BezierDerivative(first), BezierDerivative(second)) }

    fun extend(t: Double): BezierDerivative = BezierDerivative(curve.extend(t))

}

