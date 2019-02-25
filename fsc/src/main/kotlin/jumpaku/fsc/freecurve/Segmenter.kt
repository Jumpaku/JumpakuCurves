package jumpaku.fsc.freecurve

import jumpaku.core.curve.Curve
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.fsc.identify.primitive.CurveClass
import jumpaku.fsc.identify.primitive.Open4Identifier
import jumpaku.fsc.identify.primitive.reparametrize
import java.util.*

class SegmentResult(val isConicSections: Grade, val segmentParamIndices: List<Int>, val segments: List<Segment.CS>)

sealed class Segment {
    abstract val isConicSection: Grade
    abstract val curveClass: CurveClass
    class CS(override val isConicSection: Grade, override val curveClass: CurveClass, val conicSection: ConicSection) : Segment()
    class FO(override val isConicSection: Grade, override val curveClass: CurveClass, val freeCurve: BSpline) : Segment()
}


class Segmenter(val identify: (BSpline) -> Segment = defaultIdentifier) {

    class Answer(val isConicSection: Grade, val segmentIndices: List<Int>)

    data class NarrowedInterval(val begin: Int, val end: Int)

    class MemoizedIdentifier(val fsc: BSpline, val ts: List<Double>, val identify: (BSpline) -> Segment) {

        private val cache = mutableMapOf<Pair<Int, Int>, Segment>()

        operator fun invoke(i: Int, j: Int): Segment {
            require(i <= j)
            if (i == j) println("Segmenter.MemoizedIdentifier.invoke(Int, Int) i == j")
            return cache.getOrPut(i to j) {
                if (i == j) Segment.CS(Grade.TRUE, CurveClass.LineSegment, fsc(ts[i]).let { p ->
                    ConicSection(p, p, p, 1.0)
                })
                else identify(fsc.restrict(ts[i], ts[j]))
            }
        }
    }

    fun segment(fsc: BSpline, ts: List<Double>): SegmentResult {
        val identifier = MemoizedIdentifier(fsc, ts, identify)
        val (mu, ps) = segment(fsc, ts, identifier)
        val qs = ps.zipWithNext { a, b -> identifier(a, b) as Segment.CS }
        return SegmentResult(mu, ps, qs)
    }

    fun isValidSearchParams(ts: List<Double>, s: BSpline): Boolean =
            ts.zipWithNext { a, b -> identify(s.restrict(a, b)) }.all { it is Segment.CS }

    /**
     * 始終点を含む
     */
    fun segment(fsc: BSpline, ts: List<Double>, identifier: MemoizedIdentifier): Pair<Grade, List<Int>> {
        require(isValidSearchParams(ts, fsc)) { "invalid searchParams(ts.size(${ts.size}))" }

        val n = ts.size
        val narrowedIntervals = narrow(n, identifier)

        val answerTable = mutableMapOf(0 to Answer(Grade.TRUE, emptyList()))
        var indexNarrowed = 0
        (1 until n).forEach { j ->
            if (narrowedIntervals[indexNarrowed + 1].begin == j) {
                ++indexNarrowed
            }

            if (j <= narrowedIntervals[indexNarrowed].end) {
                val k = argMaxBisection(
                        narrowedIntervals[indexNarrowed - 1].begin,
                        narrowedIntervals[indexNarrowed - 1].end,
                        j,
                        answerTable,
                        identifier
                )
                if (k < narrowedIntervals[indexNarrowed - 1].begin || narrowedIntervals[indexNarrowed - 1].end < k) {
                    throw IllegalStateException(
                            "k($k) is out of [${narrowedIntervals[indexNarrowed - 1].begin},  + ${narrowedIntervals.get(
                                    indexNarrowed - 1
                            ).end}]"
                    )
                }
                val answerK = answerTable[k]!!
                val mu = answerK.isConicSection and identifier(k, j).isConicSection
                val p = listOf(k) + answerK.segmentIndices
                answerTable[j] = Answer(mu, p)
            }
        }

        return Pair(
                answerTable[n - 1]!!.isConicSection,
                (listOf(n - 1) + answerTable[n - 1]!!.segmentIndices).reversed()
        )
    }

    fun argMaxBisection(
            plFirst: Int,
            plLast: Int,
            j: Int,
            answerTable: Map<Int, Answer>,
            identifier: MemoizedIdentifier
    ): Int {
        val calcMuj = { k: Int ->
            when {
                (k < plFirst) || (plLast < k) -> 0.0
                identifier(k, j) is Segment.CS ->
                    (identifier(k, j).isConicSection and answerTable[k]!!.isConicSection).value
                else -> Double.NEGATIVE_INFINITY
            }
        }

        var a = plFirst
        var b = plLast
        while (true) {
            if (a == b) {
                return a
            }
            val k = a + Math.ceil(((b - a) / 2.0)).toInt()
            when {
                identifier(k, j) is Segment.FO -> a = k + 1
                calcMuj(k) < calcMuj(k + 1) -> a = k + 1
                calcMuj(k) <= calcMuj(k - 1) -> b = k - 1
                else -> {
                    b = k
                    a = b
                }
            }
        }
    }

    fun leftGreedyP(n: Int, identifier: MemoizedIdentifier): List<Int> =
            (0 until n).fold(listOf(0)) { ls, i ->
                when {
                    i == n - 1 -> ls + (n - 1)
                    identifier(ls.last(), i + 1) is Segment.FO -> ls + (i)
                    else -> ls
                }
            }

    fun rightGreedyP(identifier: MemoizedIdentifier, ls: List<Int>): List<Int> {
        val m = ls.size - 1
        val rs = LinkedList<Int>()
        rs.addFirst(ls[m])
        for (indexPl in (m - 1) downTo 1) {
            val rip1 = rs.first
            var ri = ls[indexPl - 1] + 1
            for (r in ls[indexPl] downTo ls[indexPl - 1] + 1) {
                if (identifier(r, rip1) is Segment.FO) {
                    ri = r + 1
                    break
                }
            }
            rs.addFirst(ri)
        }
        rs.addFirst(0)
        return rs
    }

    fun rightGreedyP(n: Int, identifier: MemoizedIdentifier): List<Int> =
            (0 until n).reversed().fold(listOf(n - 1)) { rs, i ->
                when {
                    i == 0 -> listOf(0) + rs
                    identifier(i - 1, rs.first()) is Segment.FO -> listOf(i) + rs
                    else -> rs
                }
            }

    fun leftGreedyP(identifier: MemoizedIdentifier, rs: List<Int>): List<Int> {
        val m = rs.size - 1
        val ls = LinkedList<Int>()
        ls.addLast(0)
        for (indexPr in 1 until m) {
            val lim1 = ls.last
            var li = rs[indexPr + 1] - 1
            for (l in rs[indexPr] until rs[indexPr + 1]) {
                if (identifier(lim1, l) is Segment.FO) {
                    li = l - 1
                    break
                }
            }
            ls.addLast(li)
        }
        ls.addLast(rs[m])
        return ls
    }

    fun narrow(n: Int, identifier: MemoizedIdentifier): List<NarrowedInterval> {
        val ls1 = leftGreedyP(n, identifier)
        val rs1 = rightGreedyP(identifier, ls1)
        val ss1 = rs1.zip(ls1, Segmenter::NarrowedInterval)

        val rs2 = rightGreedyP(n, identifier)
        val ls2 = leftGreedyP(identifier, rs2)
        val ss2 = rs2.zip(ls2, Segmenter::NarrowedInterval)

        return if (ss1.size < ss2.size) ss1 else ss2
    }

    companion object {

        val defaultIdentifier: (BSpline) -> Segment = { s ->
            val identifier = Open4Identifier(nSamples = 25, nFmps = 15)
            val result = identifier.identify(reparametrize(s, maxSamples = 65))
            val isCs = !result.grades[CurveClass.OpenFreeCurve]!!
            val curve: Curve = when (result.curveClass) {
                CurveClass.LineSegment -> result.linear.base
                CurveClass.CircularArc -> result.circular.base
                CurveClass.EllipticArc -> result.elliptic.base
                else -> s
            }
            if (result.curveClass.isConicSection) Segment.CS(isCs, result.curveClass, curve as ConicSection)
            else (Segment.FO(isCs, result.curveClass, curve as BSpline))
        }
    }
}
