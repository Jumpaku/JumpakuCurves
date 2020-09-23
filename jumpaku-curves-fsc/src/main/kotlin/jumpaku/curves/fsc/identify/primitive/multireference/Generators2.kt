package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.identify.primitive.multireference.AbstractSlidingReferenceElementBuilder.Companion.weights
import jumpaku.curves.fsc.identify.primitive.multireference.SlidingMultiReference.Companion.convertParam
import jumpaku.curves.fsc.identify.primitive.multireference.SlidingMultiReference.Companion.invertParam
import org.apache.commons.math3.linear.MatrixUtils
import kotlin.math.sqrt


abstract class AbstractReferenceElementBuilder2<C : Curve>(generations: Int, val fsc: ReparametrizedCurve<C>)
    : ReferenceElementBuilder<C> {

    final override val elementsSize: Int = RecursiveMultiReference.elementsCount(generations)

    abstract val representParamPointList: List<ParamPoint>

    abstract override val globalWeight: Double

    private val representIndexList: List<Triple<Int, Int, Int>> =
            partitionIndices(RecursiveMultiReference.partitionsCount(generations))

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
        return (0 until elementsSize).map { index ->
            val iFront = representFrontIndex[index]
            val iBack = representBackIndex[index]
            val w = when {
                index % 3 == 0 -> ws[index]
                else -> 2 * ws[index] * ws[index] - 1
            }
            RepresentPoints(
                    representParamPointList[iFront].point,
                    computeFar(iFront, iBack, w),//[(a + b) / 2].point,
                    representParamPointList[iBack].point
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

    override fun build(): Map<Int, ReferenceElement> {
        val ps = representPoints()
        val ts = representParams()
        val ws = weights()
        return (0 until elementsSize).associateWith { index ->
            build(QuadraticRationalBezier(ps[index], ts[index], ws[index]))
        }

    }

    companion object {

        fun partitionIndices(partitions: Int): List<Triple<Int, Int, Int>> =
                ArrayList<Triple<Int, Int, Int>>(partitions).apply {
                    for (i in 0 until partitions) {
                        val pi = (i - 1) / 2
                        when {
                            i == 0 -> add(Triple(0, (partitions + 1) / 2, partitions + 1))
                            i.isOdd() -> get(pi).let { (a, b, _) -> add(Triple(a, (a + b) / 2, b)) }
                            i.isEven() -> get(pi).let { (_, b, c) -> add(Triple(b, (b + c) / 2, c)) }
                        }
                    }
                }
    }
}

class EllipticGenerator2(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

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
            val nRepresentPoints = RecursiveMultiReference.representPointsCount(generations)
            val (w, ts) = computeGlobalEllipticParameters(fsc, nRepresentPoints, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)

    companion object {

        fun fitElliptic() {
            val generations = 3
            val size = (1 shl generations) - 1
            val representIndexList = ArrayList<Triple<Int, Int, Int>>(size).apply {
                for (i in 0 until size) {
                    val pi = (i - 1) / 2
                    when {
                        i == 0 -> add(Triple(0, (size + 1) / 2, size + 1))
                        i.isOdd() -> get(pi).let { (a, b, _) -> add(Triple(a, (a + b) / 2, b)) }
                        i.isEven() -> get(pi).let { (_, b, c) -> add(Triple(b, (b + c) / 2, c)) }
                    }
                }
            }
            val arr = IntArray(size + 2).apply {
                set(0, -1)
                representIndexList.forEachIndexed { index, (_, i, _) -> set(i, index) }
                set(size + 1, -1)
            }.toList()
            println(arr)

            val elementsSize = (1 shl (generations)) - 1
            val globalWeight = 0.0
            val weights = weights(globalWeight, elementsSize)
            val farParamList = listOf(-1 / (2 * globalWeight)) + arr.subList(1, arr.lastIndex).map {
                invertParam(it, 0.5, weights)
            } + listOf((2 * globalWeight + 1) / (2 * globalWeight))
            arr.toList().windowed(((1 shl generations) + 2) / 2) { l ->
                Triple(l.first(), l[l.size / 2], l.last())
            }.forEachIndexed { index, (a, b, c) ->
                val params = farParamList.map { convertParam(b, it, weights) }
                val d = params.size / 4
                println("$index : ($a,$b,$c) : ${params[index]},${params[index + d]},${params[index + d * 2]}")
                val representParams = RepresentParams(params[index], params[index + d], params[index + d * 2])
                val (t0, t1, t2) = representParams
                val (wt0, wt1, wt2) = representParams.map { QuadraticRationalBezier.omega(it, globalWeight) }
                val matrix = arrayOf(
                        doubleArrayOf(QuadraticRationalBezier.basisP0(t0) / wt0, QuadraticRationalBezier.basisP1(t0) * (1 + globalWeight) / wt0, QuadraticRationalBezier.basisP2(t0) / wt0),
                        doubleArrayOf(QuadraticRationalBezier.basisP0(t1) / wt1, QuadraticRationalBezier.basisP1(t1) * (1 + globalWeight) / wt1, QuadraticRationalBezier.basisP2(t1) / wt1),
                        doubleArrayOf(QuadraticRationalBezier.basisP0(t2) / wt2, QuadraticRationalBezier.basisP1(t2) * (1 + globalWeight) / wt2, QuadraticRationalBezier.basisP2(t2) / wt2)
                )
                val transform = matrix.let(MatrixUtils::createRealMatrix).let(MatrixUtils::inverse)
            }
            println(listOf(0) + representIndexList.map { it.second } + listOf(size + 1))

            println(farParamList.map { convertParam(2, it, weights) })
            QuadraticRationalBezier

        }
        /*fun coefficients(t:Double, params: RepresentParams, weight: Double): List<Double> {

        }*/

    }
}

class CircularGenerator2(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

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
            val nRepresentPoints = RecursiveMultiReference.representPointsCount(generations)
            val (w, ts) = computeGlobalCircularParameters(fsc, nRepresentPoints, 100, 10)
            globalWeight = w
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

class LinearGenerator2(generations: Int) : AbstractRecursiveReferenceGenerator(generations) {

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
            val nRepresentPoints = RecursiveMultiReference.representPointsCount(generations)
            val ts = fsc.domain.sample(nRepresentPoints).map { fsc.reparametrizer.toOriginal(it) }
            representParamPointList = ts.map { ParamPoint(fsc.originalCurve(it), it) }
        }
    }

    override fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C> =
            ElementBuilder(generations, fsc)
}

fun main() {
    EllipticGenerator2.fitElliptic()
}