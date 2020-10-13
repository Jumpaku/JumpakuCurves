package jumpaku.curves.fsc.merge

import jumpaku.commons.control.*
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import java.util.*
import kotlin.math.abs


class OverlapDetector(val overlapThreshold: Grade = Grade.FALSE) {

    fun detect(existSamples: List<ParamPoint>, overlapSamples: List<ParamPoint>, mergeRate: Double): Option<OverlapState> {
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val distMaxRidge = findRidge(osm, compareBy { it.dist(mergeRate) }) { i, j -> osm[i, j] > overlapThreshold }
                .map { it.subRidge.map { it.asPair() } }
                .filter { it.isNotEmpty() }
                .orNull() ?: return None
        val available = collectRange(osm, distMaxRidge, overlapThreshold)
        val grade = findRidge(osm, compareBy { it.grade }) { i, j -> osm[i, j] > overlapThreshold && (i to j) in available }
                .map { it.grade }.orThrow()
        val gradeMaxRidge = findRidge(osm, compareBy { it.dist(mergeRate) }) { i, j -> osm[i, j] >= grade && (i to j) in available }
                .map { it.subRidge.map { it.asPair() } }
                .orThrow()
        val range = collectRange(osm, gradeMaxRidge, overlapThreshold)
        return Some(OverlapState(osm, grade, gradeMaxRidge, range))
    }

    companion object {

        data class DpKey(val i: Int, val j: Int) {
            fun asPair(): Pair<Int, Int> = i to j
        }

        class DpValue(val grade: Grade, val subRidge: List<DpKey>) {

            fun extend(key: DpKey, keyGrade: Grade): DpValue =
                    DpValue(grade and keyGrade, subRidge + key)

            fun dist(mergeRate: Double): Double {
                val (fi, fj) = subRidge.first()
                val (li, lj) = subRidge.last()
                return abs(li - fi).toDouble().lerp(mergeRate, abs(lj - fj).toDouble())
            }
        }

        fun findRidge(osm: OverlapMatrix, compare: Comparator<DpValue>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            val dpTable = LinkedHashMap<DpKey, Option<DpValue>>()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                val muij = osm[i, j]
                when {
                    !isAvailable(i, j) -> none()
                    i == 0 && j == 0 -> some(DpValue(muij, listOf(key)))
                    i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key, muij) } + DpValue(muij, listOf(key)))
                            .maxWith(compare).toOption()
                    j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key, muij) } + DpValue(muij, listOf(key)))
                            .maxWith(compare).toOption()
                    else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, muij) } }
                            .maxWith(compare).toOption()
                }
            }

            val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
            val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
            return (right + bottom).flatMap { dpSearch(it) }.maxWith(compare).toOption()
        }

        fun collectRange(osm: OverlapMatrix, ridge: List<Pair<Int, Int>>, threshold: Grade): Set<Pair<Int, Int>> {
            if (ridge.isEmpty()) return emptySet()

            // Left Bottom
            val frontLB = ridge.first()
                    .let { (i, j) -> ((j - 1) downTo 0).takeWhile { osm[i, it] > threshold }.map { i to it } }
            val backLB = ridge.last()
                    .let { (i, j) -> ((i + 1)..osm.rowLastIndex).takeWhile { osm[it, j] > threshold }.map { it to j } }
            val pLB = (frontLB + ridge + backLB).toSet()
            val dpTableLB = HashMap<Pair<Int, Int>, Boolean>()
            fun isAvailableLB(key: Pair<Int, Int>): Boolean = dpTableLB.getOrPut(key) {
                val (i, j) = key
                when {
                    (key in pLB) -> true
                    i > 0 && j < osm.columnLastIndex && osm[i, j] > threshold ->
                        setOf((i - 1) to j, i to (j + 1), (i - 1) to (j + 1)).all { isAvailableLB(it) } ||
                                setOf((i - 1) to j, i to (j + 1)).any { it in pLB }
                    else -> false
                }
            }
            // Right Above
            val frontRA = ridge.first()
                    .let { (i, j) -> ((i - 1) downTo 0).takeWhile { osm[it, j] > threshold }.map { it to j } }
            val backRA = ridge.last()
                    .let { (i, j) -> ((j + 1)..osm.columnLastIndex).takeWhile { osm[i, it] > threshold }.map { i to it } }
            val pRA = (frontRA + ridge + backRA).toSet()
            val dpTableRA = HashMap<Pair<Int, Int>, Boolean>()
            fun isAvailableRA(key: Pair<Int, Int>): Boolean = dpTableRA.getOrPut(key) {
                val (i, j) = key
                when {
                    (key in pRA) -> true
                    i < osm.rowLastIndex && j > 0 && osm[i, j] > threshold ->
                        setOf((i + 1) to j, i to (j - 1), (i + 1) to (j - 1)).all { isAvailableRA(it) } ||
                                setOf((i + 1) to j, i to (j - 1)).any { it in pRA }
                    else -> false
                }
            }

            return (0..osm.rowLastIndex).flatMap { i ->
                (0..osm.columnLastIndex).mapNotNull { j ->
                    (i to j).takeIf { isAvailableLB(it) || isAvailableRA(it) }
                }
            }.toSet()

        }
    }
}
