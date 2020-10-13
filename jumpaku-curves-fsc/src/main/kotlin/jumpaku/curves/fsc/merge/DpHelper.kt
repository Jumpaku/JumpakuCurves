package jumpaku.curves.fsc.merge

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.toOption
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import kotlin.math.abs

object DpHelper {

    data class DpKey(val i: Int, val j: Int) {

        fun asPair(): Pair<Int, Int> = i to j
    }


    /**
     * Maximize logical conjunction of possibility uij = 1 - d/(rp_i + rq_j).
     */
    fun findRidgeBeginEnd(
            osm: OverlapMatrix,
            mergeRate: Double,
            isAvailable: (DpKey) -> Boolean
    ): Option<Pair<DpKey, DpKey>> {
        class DpValue(val grade: Grade, val path: List<DpKey>) : Comparable<DpValue> {

            val len = path.run {
                val (bi, bj) = first()
                val (ei, ej) = last()
                abs(ei - bi).toDouble().lerp(mergeRate, abs(ej - bj).toDouble())
            }

            override fun compareTo(other: DpValue): Int = compareValues(len, other.len)
        }

        fun DpValue.extend(dpKey: DpKey, grade: Grade): DpValue = DpValue(this.grade and grade, path + dpKey)

        val dpCache = LinkedHashMap<DpKey, Option<DpValue>>()
        fun dp(key: DpKey): Option<DpValue> = dpCache.getOrPut(key) {
            val (i, j) = key
            val muij = osm[i, j]
            when {
                !isAvailable(key) -> None
                i == 0 && j == 0 -> Some(DpValue(muij, listOf(key)))
                i == 0 -> (dp(DpKey(i, j - 1)).map { it.extend(key, muij) } + DpValue(muij, listOf(key))).max().toOption()
                j == 0 -> (dp(DpKey(i - 1, j)).map { it.extend(key, muij) } + DpValue(muij, listOf(key))).max().toOption()
                else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                        .flatMap { dp(it).map { value -> value.extend(key, muij) } }
                        .maxOrNull().toOption()
            }
        }

        val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
        val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
        return (right + bottom).flatMap { dp(it) }
                .max().toOption()
                .map { it.path.first() to it.path.last() }
    }

    /**
     * Minimize sum of modified distance dij = d/(rp_i + rq_j) = 1 - ( 1 - d/(rp_i + rq_j)) = 1 - uij.
     */
    fun findRidge(
            osm: OverlapMatrix,
            begin: DpKey,
            end: DpKey,
            isAvailable: (DpKey) -> Boolean
    ): Option<OverlapRidge> {
        require(begin.i == 0 || begin.j == 0)
        require(end.i == osm.rowLastIndex || end.j == osm.columnLastIndex)

        class DpValue(val dist: Double, val grade: Grade, val path: List<DpKey>) : Comparable<DpValue> {

            fun extend(dpKey: DpKey, grade: Grade): DpValue =
                    DpValue(dist + (1 - grade.value), this.grade and grade, path + dpKey)

            override fun compareTo(other: DpValue): Int = compareValues(dist, other.dist)

            fun toRidge(): OverlapRidge = OverlapRidge(grade, path.map { it.asPair() })
        }

        val dpCache = LinkedHashMap<DpKey, Option<DpValue>>()
        fun dp1(key: DpKey): Option<DpValue> = dpCache.getOrPut(key) {
            val (i, j) = key
            val uij = osm[i, j]
            val dij = 1 - uij.value
            when {
                !isAvailable(key) -> None
                key == begin -> Some(DpValue(dij, uij, listOf(key)))
                i == 0 && j == 0 -> None
                i == 0 -> (dp1(DpKey(i, j - 1)).map { it.extend(key, uij) })
                j == 0 -> (dp1(DpKey(i - 1, j)).map { it.extend(key, uij) })
                else -> listOf(DpKey(i - 1, j - 1), DpKey(i, j - 1), DpKey(i - 1, j))
                        .flatMap { dp1(it) }
                        .map { it.extend(key, uij) }
                        .min().toOption()
            }
        }
        return dp1(end).map { it.toRidge() }
    }
}