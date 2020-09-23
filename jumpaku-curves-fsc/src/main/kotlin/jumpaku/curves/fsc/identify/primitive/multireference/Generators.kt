package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.line
import jumpaku.curves.core.geom.middle
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator


/**
 * ParamPoint in AbstractReferenceElementBuilder contains *original* parameter (not arc length ratio) of fsc.
 */
abstract class AbstractReferenceElementBuilder<C : Curve>(generations: Int, val fsc: ReparametrizedCurve<C>)
    : ReferenceElementBuilder<C> {

    abstract fun partition(front: ParamPoint, back: ParamPoint): ParamPoint

    abstract fun computeFar(front: ParamPoint, back: ParamPoint): Point

    abstract fun computeWeight(front: ParamPoint, middle: ParamPoint, back: ParamPoint): Double

    val begin: ParamPoint = fsc.originalCurve.run { ParamPoint(evaluate(domain.begin), domain.begin) }

    val end: ParamPoint = fsc.originalCurve.run { ParamPoint(evaluate(domain.end), domain.end) }

    final override val elementsSize = RecursiveMultiReference.elementsCount(generations)

    override val globalWeight: Double get() = weights[0]

    private val cache: List<Triple<ParamPoint, ParamPoint, ParamPoint>>

    init {
        val nPartitions = RecursiveMultiReference.partitionsCount(generations + 1)
        //1 shl (generations + 2)) - 1
        cache = ArrayList<Triple<ParamPoint, ParamPoint, ParamPoint>>(nPartitions).apply {
            add(0, Triple(begin, partition(begin, end), end))
            for (index in 1 until nPartitions) {
                val (f, p, b) = get((index - 1) / 2)
                when {
                    index.isOdd() -> add(index, Triple(f, partition(f, p), p))
                    index.isEven() -> add(index, Triple(p, partition(p, b), b))
                }
            }
        }
    }

    private val representFront: List<ParamPoint> = ArrayList<ParamPoint>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            when {
                index % 3 == 0 -> add(index, cache[2 * (index / 3) + 1].second)
                index % 3 == 2 -> add(index, cache[index / 3].second)
                index == 1 -> add(index, cache[0].first)
                index % 6 == 1 -> add(index, get((index - 7) / 2 + 2))
                index % 6 == 4 -> add(index, get((index - 4) / 2 + 1))
            }
        }
    }

    private val representBack: List<ParamPoint> = ArrayList<ParamPoint>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            when {
                index % 3 == 0 -> add(index, cache[2 * (index / 3) + 2].second)
                index % 3 == 1 -> add(index, cache[index / 3].second)
                index == 2 -> add(index, cache[0].third)
                index % 6 == 2 -> add(index, get((index - 8) / 2 + 2))
                index % 6 == 5 -> add(index, get((index - 5) / 2 + 1))
            }
        }
    }

    private val representMiddle: List<Point> = (0 until elementsSize).map { index ->
        computeFar(representFront[index], representBack[index])
    }

    open val representPoints: List<RepresentPoints> = (0 until elementsSize).map { index ->
        RepresentPoints(representFront[index].point, representMiddle[index], representBack[index].point)
    }

    open val weights: List<Double> = ArrayList<Double>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            val gi = index / 3
            val li = index % 3
            val si = if (li == 0) gi else 2 * gi + li
            when {
                index == 0 -> add(index, computeWeight(cache[1].second, cache[0].second, cache[2].second))
                li != 0 -> add(index, computeWeight(cache[2 * si + 1].second, cache[si].second, cache[2 * si + 2].second))
                gi.isOdd() -> add(index, get(((gi - 1) / 2) * 3 + 1))
                gi.isEven() -> add(index, get(((gi - 1) / 2) * 3 + 2))
            }
        }
    }

    open val representParams: List<RepresentParams> = (0 until elementsSize).map { index ->
        val wi = weights[index]
        when {
            index % 3 == 0 -> RepresentParams(0.0, 0.5, 1.0)
            else -> RepresentParams(-1 / (2 * wi), 0.5, (2 * wi + 1) / (2 * wi))
        }
    }

    abstract fun build(bezier: QuadraticRationalBezier): ReferenceElement

    override fun build(): Map<Int, ReferenceElement> {
        return (0 until elementsSize).associateWith { index ->
            val rp = representPoints[index]
            val rt = representParams[index]
            val w = weights[index]
            build(QuadraticRationalBezier(rp, rt, w))
        }
    }
}

class LinearGenerator(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder<C>(generations, fsc) {

        override fun partition(front: ParamPoint, back: ParamPoint): ParamPoint {
            val reparametrizer = fsc.reparametrizer
            val original = fsc.originalCurve
            val sFront = reparametrizer.toArcLengthRatio(front.param)
            val sBack = reparametrizer.toArcLengthRatio(back.param)
            val tPartition = reparametrizer.toOriginal(sFront.middle(sBack))
            return ParamPoint(original(tPartition), tPartition)
        }

        override fun computeFar(front: ParamPoint, back: ParamPoint): Point {
            return front.point.middle(back.point)
        }

        override fun computeWeight(front: ParamPoint, middle: ParamPoint, back: ParamPoint): Double = 1.0

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return LinearReferenceElement(bezier)
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}


class CircularGenerator(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder<C>(generations, fsc) {

        override fun partition(front: ParamPoint, back: ParamPoint): ParamPoint {
            val reparametrizer = fsc.reparametrizer
            val original = fsc.originalCurve
            val sFront = reparametrizer.toArcLengthRatio(front.param)
            val sBack = reparametrizer.toArcLengthRatio(back.param)
            val tPartition = reparametrizer.toOriginal(sFront.middle(sBack))
            return ParamPoint(original(tPartition), tPartition)
        }

        override fun computeFar(front: ParamPoint, back: ParamPoint): Point {
            val original = fsc.originalCurve
            val tMiddle = CircularGenerator.computeCircularFar(original, front.param, back.param)
            return original(tMiddle)
        }

        override fun computeWeight(front: ParamPoint, middle: ParamPoint, back: ParamPoint): Double {
            val hh = line(front.point, back.point)
                    .tryMap { middle.point.distSquare(it) }
                    .orRecover { middle.point.distSquare(middle.middle(back).point) }
            val ll = (front.point - back.point).square() / 4
            return ((ll - hh) / (ll + hh)).coerceIn(-0.999, 0.999)
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return CircularReferenceElement(bezier)
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}


class EllipticGenerator(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder<C>(generations, fsc) {

        override fun partition(front: ParamPoint, back: ParamPoint): ParamPoint {
            val original = fsc.originalCurve
            val tPartition = EllipticGenerator.computeEllipticFar(
                    original, front.param, back.param, 100)
            return ParamPoint(original(tPartition), tPartition)
        }

        override fun computeFar(front: ParamPoint, back: ParamPoint): Point = partition(front, back).point

        override fun computeWeight(front: ParamPoint, middle: ParamPoint, back: ParamPoint): Double {
            val original = fsc.originalCurve
            return EllipticGenerator.computeEllipticWeight(
                    original, front.param, back.param, middle.param, original.domain, 100)
                    .coerceIn(-0.999, 0.999)
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return EllipticReferenceElement(bezier)
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}