package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.times
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.abs


data class RepresentPoints(val front: Point, val middle: Point, val back: Point) : AbstractList<Point>() {
    override val size: Int = 3
    override fun get(index: Int): Point = when (index) {
        0 -> front
        1 -> middle
        2 -> back
        else -> throw IndexOutOfBoundsException(index)
    }
}

data class RepresentParams(val front: Double, val middle: Double, val back: Double) : AbstractList<Double>() {
    override val size: Int = 3
    override fun get(index: Int): Double = when (index) {
        0 -> front
        1 -> middle
        2 -> back
        else -> throw IndexOutOfBoundsException(index)
    }
}

class QuadraticRationalBezier(
        val representPoints: RepresentPoints,
        val representParams: RepresentParams,
        val weight: Double
) {
    val paramRange: Interval = Interval(representParams[0], representParams[2])

    private val transform: RealMatrix

    init {
        val (t0, t1, t2) = representParams
        val (wt0, wt1, wt2) = representParams.map { omega(it, weight) }
        val matrix = arrayOf(
                doubleArrayOf(basisP0(t0) / wt0, basisP1(t0) * (1 + weight) / wt0, basisP2(t0) / wt0),
                doubleArrayOf(basisP0(t1) / wt1, basisP1(t1) * (1 + weight) / wt1, basisP2(t1) / wt1),
                doubleArrayOf(basisP0(t2) / wt2, basisP1(t2) * (1 + weight) / wt2, basisP2(t2) / wt2)
        )
        transform = matrix.let(MatrixUtils::createRealMatrix).let(MatrixUtils::inverse)
    }

    fun evaluate(t: Double): Point {
        val wt = omega(t, weight)
        val b = MatrixUtils.createRealVector(doubleArrayOf(basisP0(t) / wt, basisP1(t) * (1 + weight) / wt, basisP2(t) / wt))
        val (x0, x1, x2) = transform.preMultiply(b).toArray()
        val (p0, p1, p2) = representPoints.map { it.toVector() }
        val (r0, r1, r2) = representPoints.map { it.r }
        return Point(
                x0 * p0 + x1 * p1 + x2 * p2,
                abs(x0) * r0 + abs(x1) * r1 + abs(x2) * r2
        )
    }

    companion object {
        private fun basisP0(t: Double): Double = (1 - t) * (1 - 2 * t)
        private fun basisP1(t: Double): Double = 2 * t * (1 - t)
        private fun basisP2(t: Double): Double = t * (2 * t - 1)
        private fun omega(t: Double, w: Double): Double = basisP0(t) + basisP1(t) * (1 + w) + basisP2(t)
    }
}

interface ReferenceElement {

    val bezier: QuadraticRationalBezier
    val paramRange: Interval
    fun evaluate(t: Double): Point
}

class EllipticReferenceElement(override val bezier: QuadraticRationalBezier) : ReferenceElement {

    override val paramRange: Interval = Interval(bezier.representParams[0], bezier.representParams[2])

    val complementBezier: QuadraticRationalBezier

    init {
        val w = bezier.weight
        val rp = bezier.representPoints
        val rt = bezier.representParams
        val coef = if (paramRange.span > 1) 1 / (2 - 2 * w * w) else 1 / (1 - w)
        val c = rp[1].lerp(coef to rp[0], coef to rp[2])
        val ct = RepresentParams(MultiReference.complementParam(rt[0]), 0.5, MultiReference.complementParam(rt[2]))
        val cp = RepresentPoints(rp[0], c, rp[2])
        complementBezier = QuadraticRationalBezier(cp, ct, -w)
    }

    override fun evaluate(t: Double): Point = when (t) {
        in paramRange -> bezier.evaluate(t)
        else -> complementBezier.evaluate(MultiReference.complementParam(t))
    }
}

class CircularReferenceElement(bezier: QuadraticRationalBezier)
    : ReferenceElement by EllipticReferenceElement(bezier)

class LinearReferenceElement(override val bezier: QuadraticRationalBezier) : ReferenceElement {

    override val paramRange: Interval = Interval(bezier.representParams[0], bezier.representParams[2])

    override fun evaluate(t: Double): Point = bezier.evaluate(t)
}