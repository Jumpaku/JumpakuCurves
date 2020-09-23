package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.fsc.identify.primitive.multireference.core.MultiPoint

class SlidingMultiReference(
        override val generations: Int,
        override val domain: Interval,
        elementsMap: Map<Int, ReferenceElement>) : MultiReference, Map<Int, ReferenceElement> by elementsMap {

    val elements: List<ReferenceElement> = elementsMap.toSortedMap().map { it.value }

    init {
        require(elementsCount(generations) == elements.size) { "elements.size mismatch with generations" }
    }

    private val weights = AbstractSlidingReferenceElementBuilder.weights(
            elements[0].bezier.weight,
            partitionsCount(generations)
    )
    override fun evaluate(t: Double): MultiPoint {
        require(t in domain) { "t($t) must be in domain($domain)" }
        val u = RecursiveMultiReference.unboundParam(t)
        val points = this.map { (index, r) ->
            val h = convertParam(index, u, weights)
            r.evaluate(h)
        }
        return MultiPoint(points)
    }

    companion object {

        fun elementsCount(generations: Int): Int = (1 shl generations) + 1

        fun representPointsCount(generations: Int): Int = (1 shl (generations + 1)) + 1

        fun partitionsCount(generations: Int): Int = (1 shl (generations + 1)) - 1

        fun convertParam(i: Int, u: Double, weights: List<Double>): Double {
            fun convertParam(i: Int, u: Double): Double {
                val pi = (i - 1) / 2
                val wi = weights[i]
                val wpi = 2 * wi * wi - 1
                val w = (wpi + 1) * (wi + 1) / (2 * wi)
                return when {
                    i == 0 -> u
                    i.isOdd() -> 0.5 + w / ((wpi - 1) + 1 / (convertParam(pi, u)))
                    i.isEven() -> 0.5 - w / ((wpi - 1) + 1 / (1 - convertParam(pi, u)))
                    else -> error("")
                }
            }
            return convertParam(i, u)
        }

        fun invertParam(i: Int, h: Double, weights: List<Double>): Double {
            val indices = ArrayList<Int>()
            var j = i
            while (j > 0) {
                indices.add(j)
                val pj = (j - 1) / 2
                j = when {
                    j.isOdd() -> pj
                    j.isEven() -> pj
                    else -> error("")
                }
            }
            indices.add(0)
            indices.reverse()

            fun invertParam(j: Int): Double {
                val p = indices[j]
                val c = indices[j + 1]
                val wp = weights[p]
                val wc = weights[c]
                val w = ((wc + 1) * (wp + 1)) / (2 * wc)
                val hc = if (c == i) h else invertParam(j + 1)
                return when (c) {
                    p * 2 + 1 -> 1 / ((1 - wp) + w / (hc - 0.5))
                    p * 2 + 2 -> 1 - 1 / ((1 - wp) - w / (hc - 0.5))
                    else -> error("")
                }
            }
            return if (i == 0) h else invertParam(0)
        }
    }
}