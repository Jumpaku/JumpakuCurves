package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade


data class OverlappingMatrix(val matrix: Array<Array<Grade>>) {

    constructor(samplingSpan: Double, existing: BSpline, overlapping: BSpline): this(kotlin.run {
        val existingTimes = existing.domain.sample(samplingSpan)
        val overlappingTimes = overlapping.domain.sample(samplingSpan)
        existingTimes.map { et ->
            overlappingTimes.map { ot ->
                existing(et).isPossible(overlapping(ot))
            }
        }
    })

    val rowSize: Int = matrix.size()

    val rowLastIndex: Int = rowSize - 1

    val columnSize: Int = matrix.head().size()

    val columnLastIndex: Int = columnSize - 1

    operator fun get(i: Int, j: Int): Grade = matrix[i][j]
}

