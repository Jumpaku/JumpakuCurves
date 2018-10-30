package jumpaku.fsc.blend

import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.util.Option
import jumpaku.core.util.none
import jumpaku.core.util.some
import jumpaku.core.util.toOption


class OverlapState private constructor(val osm: OverlapMatrix, val paths: List<OverlapPath>) {

    companion object {

        private fun searchInitials(osm: OverlapMatrix): List<Pair<Int, Int>> {
            data class IndexedGrade(val i: Int, val j: Int, val grade: Grade)
            val bottom = (0 until osm.columnSize)
                    .map { j -> IndexedGrade(osm.rowLastIndex, j, osm[osm.rowLastIndex, j]) }
                    .distinctBy { it.grade }
                    .windowed(3, partialWindows = true)
                    .flatMap { indexedGrades ->
                        when {
                            indexedGrades.size == 2 -> {
                                val (prev, it) = indexedGrades
                                if (prev.grade < it.grade) some(it.run { Pair(i, j) })
                                else none()
                            }
                            indexedGrades.size == 3 -> {
                                val (prev, it, next) = indexedGrades
                                if (prev.grade < it.grade && it.grade > next.grade) some(next.run { Pair(i, j - 1) })
                                else if (prev.j == 0 && prev.grade > it.grade) some(it.run { Pair(i, j - 1) })
                                else none()
                            }
                            else -> none()
                        }
                    }
            val right = (0 until osm.rowSize)
                    .map { i -> IndexedGrade(i, osm.columnLastIndex, osm[i, osm.columnLastIndex]) }
                    .distinctBy { it.grade }
                    .windowed(3, partialWindows = true)
                    .flatMap { indexedGrades ->
                        when {
                            indexedGrades.size == 2 -> {
                                val (prev, it) = indexedGrades
                                if (prev.grade < it.grade) some(it.run { Pair(i, j) })
                                else none()
                            }
                            indexedGrades.size == 3 -> {
                                val (prev, it, next) = indexedGrades
                                if (prev.grade < it.grade && it.grade > next.grade) some(next.run { Pair(i - 1, j) })
                                else if (prev.i == 0 && prev.grade > it.grade) some(it.run { Pair(i - 1, j) })
                                else none()
                            }
                            else -> none()
                        }
                    }
            return (bottom + right)
        }

        fun findPaths(osm: OverlapMatrix): List<OverlapPath> {
            val elementsList = searchInitials(osm).flatMap { initial ->
                val elements = mutableListOf<Pair<Int, Int>>()
                var index = initial
                loop@ while (true) {
                    val (i, j) = index
                    val muij = osm[i, j]
                    elements.add(0, index)
                    index = when {
                        muij <= Grade.FALSE -> return@flatMap none<List<Pair<Int, Int>>>()
                        (i == 0 && j == 0) || (i == 0 && osm[i, j - 1] < muij) || (j == 0 && osm[i - 1, j] < muij) -> break@loop
                        i == 0 -> Pair(i, j - 1)
                        j == 0 -> Pair(i - 1, j)
                        else -> listOf(Pair(i - 1, j - 1), Pair(i - 1, j), Pair(i, j - 1)).maxBy { (i, j) -> osm[i, j] }!!
                    }
                }
                some(elements)
            }
            return elementsList.map { elements ->
                val type = OverlapType.judgeType(osm.rowSize, osm.columnSize, elements)
                val grade = elements.fold(Grade.TRUE) { grade, (i, j) -> grade and osm[i, j] }
                OverlapPath(type, grade, elements)
            }
        }

        fun create(existSamples: List<Point>, overlapSamples: List<Point>): OverlapState {
            val osm = OverlapMatrix.create(existSamples, overlapSamples)
            val paths = findPaths(osm)
            return OverlapState(osm, paths)
        }
    }
}