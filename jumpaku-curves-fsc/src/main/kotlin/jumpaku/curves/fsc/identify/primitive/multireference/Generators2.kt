package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.Point
import kotlin.math.sqrt


abstract class AbstractReferenceElementBuilder2<C : Curve>(generations: Int, val fsc: ReparametrizedCurve<C>)
    : ReferenceElementBuilder<C> {

    final override val elementsSize: Int = ((1 shl generations) - 1) * 3

    abstract val representParamPointList: List<ParamPoint>

    abstract override val globalWeight: Double

    private val representIndexList: List<Triple<Int, Int, Int>>

    init {
        val size = (1 shl (generations + 2)) - 1
        representIndexList = ArrayList<Triple<Int, Int, Int>>(size).apply {
            for (i in 0 until size) {
                val pi = (i - 1) / 2
                when {
                    i == 0 -> add(Triple(0, (size + 1) / 2, size + 1))
                    i.isOdd() -> get(pi).let { (a, b, _) -> add(Triple(a, (a + b) / 2, b)) }
                    i.isEven() -> get(pi).let { (_, b, c) -> add(Triple(b, (b + c) / 2, c)) }
                }
            }
        }
    }

    private val representFrontIndex: List<Int> = ArrayList<Int>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            when {
                index % 3 == 0 -> add(index, representIndexList[2 * (index / 3) + 1].second)
                index % 3 == 2 -> add(index, representIndexList[index / 3].second)
                index == 1 -> add(index, representIndexList[0].first)
                index % 6 == 1 -> add(index, get((index - 7) / 2 + 2))
                index % 6 == 4 -> add(index, get((index - 4) / 2 + 1))
            }
        }
    }

    private val representBackIndex: List<Int> = ArrayList<Int>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            when {
                index % 3 == 0 -> add(index, representIndexList[2 * (index / 3) + 2].second)
                index % 3 == 1 -> add(index, representIndexList[index / 3].second)
                index == 2 -> add(index, representIndexList[0].third)
                index % 6 == 2 -> add(index, get((index - 8) / 2 + 2))
                index % 6 == 5 -> add(index, get((index - 5) / 2 + 1))
            }
        }
    }

    fun weights(): List<Double> = ArrayList<Double>(elementsSize).apply {
        for (index in 0 until elementsSize) {
            val gi = index / 3
            val li = index % 3
            when {
                index == 0 -> add(index, globalWeight)
                li != 0 -> add(index, sqrt((get(index - li) + 1) / 2))
                gi.isOdd() -> add(index, get(((gi - 1) / 2) * 3 + 1))
                gi.isEven() -> add(index, get(((gi - 1) / 2) * 3 + 2))
            }
        }
    }

    fun representPoints(): List<RepresentPoints> {
        val ws = weights()
        println(ws)
        return (0 until elementsSize).map { index ->
            val a = representFrontIndex[index]
            val b = representBackIndex[index]
            val w = when {
                index % 3 == 0 -> ws[index]
                else -> 2 * ws[index] * ws[index] - 1
            }
            RepresentPoints(
                    representParamPointList[a].point,
                    computeFar(a, b, w),//[(a + b) / 2].point,
                    representParamPointList[b].point
            )
        }
    }

    fun representParams(): List<RepresentParams> = weights().mapIndexed { index, wi ->
        when {
            index % 3 == 0 -> RepresentParams(0.0, 0.5, 1.0)
            else -> RepresentParams(-1 / (2 * wi), 0.5, (2 * wi + 1) / (2 * wi))
        }
    }

    abstract fun build(bezier: QuadraticRationalBezier): ReferenceElement

    abstract fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point

    override fun build(): List<ReferenceElement> {
        val ps = representPoints()
        val ts = representParams()
        val ws = weights()
        return (0 until elementsSize).map { index ->
            build(QuadraticRationalBezier(ps[index], ts[index], ws[index]))
        }

    }
}

class EllipticGenerator2(generations: Int) : AbstractReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder2<C>(generations, fsc) {

        override fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point {
            return representParamPointList[(frontIndex + backIndex) / 2].point
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return EllipticReferenceElement(bezier)
        }

        override val representParamPointList: List<ParamPoint>

        override val globalWeight: Double

        init {
            val (w, ts) = computeGlobalEllipticParameters(fsc, generations, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

class CircularGenerator2(generations: Int) : AbstractReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder2<C>(generations, fsc) {

        override fun computeFar(frontIndex: Int, backIndex: Int, weight: Double): Point {
            val (front, tFront) = representParamPointList[frontIndex]
            val (back, tBack) = representParamPointList[backIndex]
            val far = computeLocalCircularFar(fsc, tFront, tBack).point
            val m = front.middle(back)
            val t = sqrt((front.distSquare(m) * (1 - weight)).divOrDefault(far.distSquare(m) * (1 + weight)) { 0.0 })
            val f = m.lerp(t, far)
            return f
        }

        override fun build(bezier: QuadraticRationalBezier): ReferenceElement {
            return CircularReferenceElement(bezier)
        }

        override val representParamPointList: List<ParamPoint>

        override val globalWeight: Double

        init {
            val (w, ts) = computeGlobalCircularParameters(fsc, generations, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

class LinearGenerator2(generations: Int) : AbstractReferenceGenerator(generations) {

    class ElementBuilder<C : Curve>(generations: Int, fsc: ReparametrizedCurve<C>)
        : AbstractReferenceElementBuilder2<C>(generations, fsc) {

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
            val nFars = (1 shl (generations + 2)) + 1
            val ts = fsc.domain.sample(nFars).map { fsc.reparametrizer.toOriginal(it) }
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}
