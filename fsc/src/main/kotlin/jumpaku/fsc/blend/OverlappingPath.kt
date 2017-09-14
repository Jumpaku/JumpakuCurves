package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2

data class OverlappingPath(
        val osm: OverlappingMatrix,
        val grade: Grade,
        val path: Array<Tuple2<Int, Int>>){

    fun extend(grade: Grade, i: Int, j: Int): OverlappingPath {
        return when {
            (this.grade and grade) <= Grade.FALSE -> osm.emptyPath()
            else -> OverlappingPath(osm, this.grade and grade, path.append(Tuple2(i, j)))
        }
    }

    fun nonEmpty(): Boolean = path.nonEmpty()

    fun overlappingCase(): OverlappingCase {
        require(nonEmpty()) { "empty path" }
        val existingLast = osm.rowLastIndex
        val overlappingLast = osm.columnLastIndex
        val (beginI, beginJ) = path.head()
        val (endI, endJ) = path.last()

        return when{
            beginI == 0 && endI == existingLast -> OverlappingCase.OverlappingExistingOverlapping
            beginI == 0 && endJ == overlappingLast -> OverlappingCase.OverlappingExisting
            beginJ == 0 && endI == existingLast -> OverlappingCase.ExistingOverlapping
            beginJ == 0 && endJ == overlappingLast -> OverlappingCase.ExistingOverlappingExisting
            else -> error("error")
        }
    }
}

