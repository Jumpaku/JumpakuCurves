package jumpaku.curves.experimental.demo.edit.oldblend

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point


class OverlapMatrix private constructor(grades: List<List<Grade>>) {

    init {
        require(grades.map { it.size }.distinct().size == 1) { "grades rows must have equivalent sizes" }
    }

    private val grades: List<List<Grade>> = grades.map { it.toList() }

    val rowSize: Int = grades.size

    val rowLastIndex: Int = rowSize - 1

    val columnSize: Int = grades.first().size

    val columnLastIndex: Int = columnSize - 1

    operator fun get(i: Int, j: Int): Grade = grades[i][j]

    companion object {

        fun create(existSamples: List<Point>, overlapSamples: List<Point>): OverlapMatrix =
                OverlapMatrix(existSamples.map { ep -> overlapSamples.map { op -> ep.isPossible(op) } })

    }
}