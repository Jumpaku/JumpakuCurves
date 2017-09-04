package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import jumpaku.core.fuzzy.Grade

data class OverlappingPath(
        val grade: Grade,
        val path: Array<Tuple2<Int, Int>>) {

    fun extend(grade: Grade, i: Int, j: Int): OverlappingPath {
        return when {
            (this.grade and grade) <= Grade.FALSE -> emptyPath()
            else -> OverlappingPath(this.grade and grade, path.append(Tuple2(i, j)))
        }
    }

    fun nonEmpty(): Boolean = path.nonEmpty()
}

fun emptyPath() = OverlappingPath(Grade.FALSE, Array.empty())

fun initialPath(grade: Grade, i: Int, j: Int): OverlappingPath {
    require(i == 0 || j == 0) { "index i($i) or j($j) are not beginning index" }
    return when {
        grade <= Grade.FALSE -> emptyPath()
        else -> OverlappingPath(grade, Array.of(Tuple2(i, j)))
    }
}
