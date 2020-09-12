package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.divOrDefault
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.Point
import kotlin.math.sqrt

abstract class AbstractSlidingReferenceElementBuilder<C : Curve>(generations: Int, val fsc: ReparametrizedCurve<C>)
    : ReferenceElementBuilder<C> {

    private val representPointsSize: Int = SlidingMultiReference.representPointsCount(generations)

    final override val elementsSize: Int = SlidingMultiReference.elementsCount(generations)

    abstract val representParamPointList: List<ParamPoint>

    abstract override val globalWeight: Double

    private val partitionIndices: List<Triple<Int, Int, Int>> =
            AbstractReferenceElementBuilder2.partitionIndices(SlidingMultiReference.partitionsCount(generations))

    fun representPoints(): Map<Int, RepresentPoints> {
        val indices = representParamPointList.indices
        val points = representParamPointList.map { it.point }
        val ws = weights()
        return partitionIndices.mapIndexedNotNull { elementIndex, (_, iMiddle, _) ->
            val iFront = iMiddle - (elementsSize / 2)
            val iBack = iMiddle + (elementsSize / 2)
            if (iFront !in indices || iBack !in indices) return@mapIndexedNotNull null

            val middle = computeFar(iFront, iBack, globalWeight)
            elementIndex to RepresentPoints(points[iFront], middle, points[iBack])
        }.toMap()
    }

    fun representParams(): Map<Int, RepresentParams> {
        val indices = representParamPointList.indices
        val weights = weights(globalWeight, representParamPointList.size)
        val rootParams = DoubleArray(partitionIndices.size + 2).apply {
            set(0, -1 / (2 * globalWeight))
            set(lastIndex, (2 * globalWeight + 1) / (2 * globalWeight))
            partitionIndices.forEachIndexed { elementIndex, (_, iMiddle, _) ->
                set(iMiddle, SlidingMultiReference.invertParam(elementIndex, 0.5, weights))
            }
        }
        return partitionIndices.mapIndexedNotNull { elementIndex, (_, iMiddle, _) ->
            val iFront = iMiddle - (elementsSize / 2)
            val iBack = iMiddle + (elementsSize / 2)
            if (iFront !in indices || iBack !in indices) return@mapIndexedNotNull null

            val hFront = SlidingMultiReference.convertParam(elementIndex, rootParams[iFront], weights)
            val hBack = SlidingMultiReference.convertParam(elementIndex, rootParams[iBack], weights)
            val hMiddle = SlidingMultiReference.convertParam(elementIndex, rootParams[iMiddle], weights)
            elementIndex to RepresentParams(hFront, hMiddle, hBack)
        }.toMap()
    }

    fun weights(): Map<Int, Double> {
        val indices = representParamPointList.indices
        val weights = DoubleArray(representParamPointList.size) { -1.0 }
        return partitionIndices.mapIndexedNotNull { elementIndex, (_, iMiddle, _) ->
            val iFront = iMiddle - (elementsSize / 2)
            val iBack = iMiddle + (elementsSize / 2)
            if (iFront !in indices || iBack !in indices) return@mapIndexedNotNull null

            if (elementIndex == 0) weights[elementIndex] = globalWeight
            else weights[elementIndex] = sqrt((weights[(elementIndex - 1) / 2] + 1) / 2)
            elementIndex to weights[elementIndex]
        }.toMap()
    }

    abstract fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point

    abstract fun build(bezier: QuadraticRationalBezier): ReferenceElement

    override fun build(): Map<Int, ReferenceElement> {
        val ps = representPoints()
        val ts = representParams()
        val ws = weights()
        check(ps.keys == ts.keys && ts.keys == ws.keys)
        return ps.keys.associateWith { elementIndex ->
            build(QuadraticRationalBezier(
                    ps[elementIndex]!!,
                    ts[elementIndex]!!,
                    ws[elementIndex]!!))
        }
    }

    companion object {

        fun weights(globalWeight: Double, elementsSize: Int): List<Double> = ArrayList<Double>(elementsSize).apply {
            for (index in 0 until elementsSize) {
                val w = if (index == 0) globalWeight
                else sqrt((get((index - 1) / 2) + 1) / 2)
                add(index, w)
            }
        }
    }
}

abstract class AbstractSlidingReferenceGenerator(override val generations: Int): MultiReferenceGenerator {

    abstract fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C>

    override fun <C : Curve> generate(fsc: ReparametrizedCurve<C>): SlidingMultiReference {
        val builder = createElementBuilder(fsc)
        val elements = builder.build()
        val w = builder.globalWeight
        val domain = Interval(-1 / (2 * w + 2), (2 * w + 3) / (2 * w + 2))
        return SlidingMultiReference(generations, domain, elements)
    }
}

class EllipticGenerator3(generations: Int) : AbstractSlidingReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractSlidingReferenceElementBuilder<C>(generations, fsc) {

        override fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point {
            return representParamPointList[(frontIndex + backIndex) / 2].point
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return EllipticReferenceElement(bezier)
        }

        override val representParamPointList: List<ParamPoint>

        override val globalWeight: Double

        init {
            val nRepresentPoints = SlidingMultiReference.representPointsCount(generations)
            val (w, ts) = computeGlobalEllipticParameters(fsc, nRepresentPoints, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

class CircularGenerator3(generations: Int) : AbstractSlidingReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractSlidingReferenceElementBuilder<C>(generations, fsc) {

        override fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point {
            val (front, tFront) = representParamPointList[frontIndex]
            val (back, tBack) = representParamPointList[backIndex]
            val far = computeLocalCircularFar(fsc, tFront, tBack).point
            val m = front.middle(back)
            val t = sqrt((front.distSquare(m) * (1 - weight)).divOrDefault(far.distSquare(m) * (1 + weight)) { 0.0 })
            return m.lerp(t, far)
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return CircularReferenceElement(bezier)
        }

        override val representParamPointList: List<ParamPoint>

        override val globalWeight: Double

        init {
            val nRepresentPoints = SlidingMultiReference.representPointsCount(generations)
            val (w, ts) = computeGlobalCircularParameters(fsc, nRepresentPoints, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

class LinearGenerator3(generations: Int) : AbstractSlidingReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractSlidingReferenceElementBuilder<C>(generations, fsc) {

        override fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point {
            val (front, _) = representParamPointList[frontIndex]
            val (back, _) = representParamPointList[backIndex]
            return front.middle(back)
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return LinearReferenceElement(bezier)
        }

        override val representParamPointList: List<ParamPoint>

        override val globalWeight: Double = 1.0

        init {
            val nRepresentPoints = SlidingMultiReference.representPointsCount(generations)
            val ts = fsc.domain.sample(nRepresentPoints).map { fsc.reparametrizer.toOriginal(it) }
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}
