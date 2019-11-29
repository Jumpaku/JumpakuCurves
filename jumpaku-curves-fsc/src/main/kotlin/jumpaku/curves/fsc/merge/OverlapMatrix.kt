package jumpaku.curves.fsc.merge

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point


class OverlapMatrix private constructor(grades: List<List<Grade>>) {

    init {
        require(grades.distinctBy { it.size }.size == 1) { "grades rows must have equivalent sizes" }
    }

    private val grades: List<List<Grade>> = grades.map { it.toList() }

    val rowSize: Int = grades.size

    val rowLastIndex: Int = rowSize - 1

    val columnSize: Int = grades.first().size

    val columnLastIndex: Int = columnSize - 1

    operator fun get(i: Int, j: Int): Grade = grades[i][j]

    operator fun get(key: Pair<Int, Int>): Grade = get(key.first, key.second)

    companion object {

        fun create(existSamples: List<Point>, overlapSamples: List<Point>): OverlapMatrix =
                OverlapMatrix(existSamples.map { ep -> overlapSamples.map { op -> ep.isPossible(op) } })

    }
}