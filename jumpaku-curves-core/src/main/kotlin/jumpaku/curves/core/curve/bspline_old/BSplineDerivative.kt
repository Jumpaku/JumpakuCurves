package jumpaku.curves.core.curve.bspline_old


import jumpaku.commons.control.Option
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bezier.BezierDerivative
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector


class BSplineDerivative(bSpline: BSpline) : Derivative, Differentiable {

    constructor(controlVectors: Iterable<Vector>, knots: KnotVector) : this(
            BSpline(controlVectors.map { Point.xyz(it.x, it.y, it.z) }, knots))

    val curve: BSpline = bSpline.toCrisp()

    override val domain: Interval get() = curve.domain

    override fun differentiate():  BSplineDerivative = curve.differentiate()

    val controlVectors: List<Vector> get() = curve.controlPoints.map(Point::toVector)

    val knotVector: KnotVector get() = curve.knotVector

    val degree: Int get() = curve.degree

    override fun toString(): String = "BSplineDerivative(knotVector=$knotVector, controlVectors=$controlVectors)"

    override fun evaluate(t: Double): Vector = curve(t).toVector()

    fun restrict(begin: Double, end: Double): BSplineDerivative = BSplineDerivative(curve.restrict(begin, end))

    fun restrict(i: Interval): BSplineDerivative = BSplineDerivative(curve.restrict(i))

    fun reverse(): BSplineDerivative = BSplineDerivative(curve.reverse())

    fun clamp(): BSplineDerivative = BSplineDerivative(curve.clamp())

    fun close(): BSplineDerivative = BSplineDerivative(curve.close())

    fun insertKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(curve.insertKnot(t, m))

    fun removeKnot(t: Double, m: Int = 1): BSplineDerivative = BSplineDerivative(curve.removeKnot(t, m))

    fun removeKnot(knotIndex: Int, m: Int = 1): BSplineDerivative = BSplineDerivative(curve.removeKnot(knotIndex, m))

    fun toBeziers(): List<BezierDerivative> = curve.toBeziers().map(::BezierDerivative)

    fun subdivide(t: Double): Pair<Option<BSplineDerivative>, Option<BSplineDerivative>> = curve.subdivide(t)
            .run { Pair(first.map { BSplineDerivative(it) }, second.map { BSplineDerivative(it) }) }
}
