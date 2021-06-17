package jumpaku.curves.fsc.blend

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point


class OverlapMatrix private constructor(val rowSize: Int, val columnSize: Int, val buffer: DoubleArray) {

    data class Key(val row: Int, val column: Int)

    val rowLastIndex: Int = rowSize - 1

    val columnLastIndex: Int = columnSize - 1

    init {
        require(buffer.size == rowSize * columnSize) { "grades rows must have equivalent sizes" }
    }

    operator fun get(i: Int, j: Int): Grade {
        require(i in 0 until rowSize) { "i($i) must be in ${0 until rowSize}" }
        require(j in 0 until columnSize) { "j($j) must be in ${0 until columnSize}" }
        return Grade(buffer[i * columnSize + j])
    }

    operator fun get(key: Key): Grade = get(key.row, key.column)

    companion object {

        fun create(existSamples: List<Point>, overlapSamples: List<Point>): OverlapMatrix {
            val rowSize = existSamples.size
            val columnSize = overlapSamples.size
            val buffer = DoubleArray(existSamples.size * overlapSamples.size) { 0.0 }.apply {
                existSamples.forEachIndexed { i, ep ->
                    overlapSamples.forEachIndexed { j, op ->
                        set(i * columnSize + j, ep.isPossible(op).value)
                    }
                }

            }
            return OverlapMatrix(rowSize, columnSize, buffer)
        }
    }
}