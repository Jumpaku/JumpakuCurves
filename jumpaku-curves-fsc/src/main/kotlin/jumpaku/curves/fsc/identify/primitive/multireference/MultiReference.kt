package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.fsc.identify.primitive.multireference.core.MultiCurve
import jumpaku.curves.fsc.identify.primitive.multireference.core.MultiPoint


class MultiReference(
        val generations: Int,
        override val domain: Interval,
        val elements: List<ReferenceElement>
) : MultiCurve {

    init {
        val n = ((1 shl generations) - 1) * 3
        require(n == elements.size) { "elements.size mismatch with generations" }
    }

    fun weight(i: Int): Double = elements[i].bezier.weight

    fun convertParam(i: Int, u: Double): Double {
        val gi = i / 3
        val li = i % 3
        val pi = 3 * ((gi - 1) / 2)
        val wi = weight(i)
        val wpi = if (li == 0) weight(pi) else weight(i - li)
        val w = (wpi + 1) * (wi + 1) / (2 * wi)
        return when {
            i == 0 -> u//unboundParam(u)
            li == 0 && gi.isOdd() -> 0.5 + w / ((wpi - 1) + 1 / (convertParam(pi, u)))
            li == 0 && gi.isEven() -> 0.5 - w / ((wpi - 1) + 1 / (1 - convertParam(pi, u)))
            li == 1 -> 0.5 + w / ((wpi - 1) + 1 / (convertParam(i - 1, u)))
            li == 2 -> 0.5 - w / ((wpi - 1) + 1 / (1 - convertParam(i - 2, u)))
            li != 0 -> convertParam(2 * i + li, u)
            else -> error("")
        }
    }

    fun invertParam(i: Int, h: Double): Double {
        val ws = ArrayList<Double>()
        val indices = ArrayList<Int>()
        var j = i
        while (j > 0) {
            indices.add(j)
            val gj = j / 3
            val lj = j % 3
            val pj = 3 * ((gj - 1) / 2)
            ws.add(if (lj == 0) weight(pj) else weight(i - lj))
            j = when {
                lj == 0 && gj.isOdd() -> pj
                lj == 0 && gj.isEven() -> pj
                lj == 1 -> i - 1
                lj == 2 -> i - 2
                lj != 0 -> 2 * i + lj
                else -> error("")

            }
        }
        indices.add(0)
        indices.reverse()
        ws.reverse()
        ws.add(weight(i))

        fun invertParam(j: Int): Double {
            val p = indices[j]
            val c = indices[j + 1]
            val wp = ws[j]
            val wc = ws[j + 1]
            val w = ((wc + 1) * (wp + 1)) / (2 * wc)
            val hc = if (c == i) h else invertParam(j + 1)
            val gc = c / 3
            val gp = p / 3
            return when {
                (c - 1 == p) || (gc - 1) == gp * 2 -> 1 / ((1 - wp) + w / (hc - 0.5))
                (c - 2 == p) || (gc - 2) == gp * 2 -> 1 - 1 / ((1 - wp) - w / (hc - 0.5))
                else -> error("")
            }
        }
        //return boundParam(if (i == 0) h else invertParam(0))
        return if (i == 0) h else invertParam(0)
    }

    override fun evaluate(t: Double): MultiPoint {
        require(t in domain) { "t($t) must be in domain($domain)" }
        val points = elements.mapIndexed { index, e ->
            e.evaluate(convertParam(index, unboundParam(t)))
        }
        return MultiPoint(points)
    }

    companion object {

        fun complementParam(h: Double): Double = 1 / (2 - 1 / h)

        fun unboundParam(t: Double): Double = when {
            t < 0 -> 1 / (2 + 1 / t)
            t > 1 -> (1 - 2 / t) / (2 - 3 / t)
            else -> t
        }

        fun boundParam(u: Double): Double = when {
            u < 0 -> 1 / (1 / u - 2)
            u > 1 -> (3 - 2 / u) / (2 - 1 / u)
            else -> u
        }
    }
}