package jumpaku.fsc.blend

import io.vavr.Function2
import io.vavr.collection.Array
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.function2
import jumpaku.core.util.lastIndex

class OverlappingMatrix(samplingSpan: Double, val existing: BSpline, val overlapping: BSpline){

    val existingTimes: Array<Double> = existing.domain.sample(samplingSpan)

    val overlappingTimes: Array<Double> = overlapping.domain.sample(samplingSpan)

    private val computeGrade: Function2<Int, Int, Grade> = function2 { i: Int, j: Int ->
        existing(existingTimes[i]).isPossible(overlapping(overlappingTimes[j]))
    }.memoized()

    operator fun get(i: Int, j: Int): Grade = computeGrade.apply(i, j)

    fun overlappingCase(path: OverlappingPath): OverlappingCase {
        require(path.nonEmpty()) { "empty path" }
        val existingLast = existingTimes.lastIndex
        val overlappingLast = overlappingTimes.lastIndex
        val (beginI, beginJ) = path.path.head()
        val (endI, endJ) = path.path.last()

        return when{
            beginI == 0 && endI == existingLast -> OverlappingCase.OverlappingExistingOverlapping
            beginI == 0 && endJ == overlappingLast -> OverlappingCase.OverlappingExisting
            beginJ == 0 && endI == existingLast -> OverlappingCase.ExistingOverlapping
            beginJ == 0 && endJ == overlappingLast -> OverlappingCase.ExistingOverlappingExisting
            else -> error("invalid path($path) (osm(${existingLast - 1}x${overlappingLast - 1}))")
        }
    }
}