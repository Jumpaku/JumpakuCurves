package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Stream
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.Option
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.optionWhen

data class OverlappingPath(
        val type: OverlappingType,
        val grade: Grade,
        val path: Array<Tuple2<Int, Int>>) {

    class Builder(
            val osm: OverlappingMatrix,
            val grade: Grade = Grade.FALSE,
            val path: Array<Tuple2<Int, Int>> = Array.empty()) {

        fun extendPath(i: Int, j: Int): Builder {
            val mu = osm[i, j] and grade
            return if (mu > Grade.FALSE) Builder(osm, mu, path.append(Tuple2(i, j))) else Builder(osm)
        }

        fun build(): Option<OverlappingPath> = optionWhen(path.nonEmpty()) {
             OverlappingPath(overlappingType(osm, path), grade, path)
        }
    }
}


enum class OverlappingType{
    ExistingOverlapping,
    OverlappingExisting,
    ExistingOverlappingExisting,
    OverlappingExistingOverlapping
}

fun overlappingType(osm: OverlappingMatrix, path: Array<Tuple2<Int, Int>>): OverlappingType {
    val existingLast = osm.rowLastIndex
    val overlappingLast = osm.columnLastIndex
    val (beginI, beginJ) = path.head()
    val (endI, endJ) = path.last()

    return when{
        beginI == 0 && endI == existingLast -> OverlappingType.OverlappingExistingOverlapping
        beginI == 0 && endJ == overlappingLast -> OverlappingType.OverlappingExisting
        beginJ == 0 && endI == existingLast -> OverlappingType.ExistingOverlapping
        beginJ == 0 && endJ == overlappingLast -> OverlappingType.ExistingOverlappingExisting
        else -> error("")
    }
}

data class OverlappingMatrix(val matrix: Array<Array<Grade>>) {

    val rowSize: Int = matrix.size()

    val rowLastIndex: Int = rowSize - 1

    val columnSize: Int = matrix.head().size()

    val columnLastIndex: Int = columnSize - 1

    operator fun get(i: Int, j: Int): Grade = matrix[i][j]

    fun searchPath(evaluatePath: OverlappingMatrix.(OverlappingPath) -> Grade): Option<OverlappingPath> {
        var dpTable = HashMap.empty<Tuple2<Int, Int>, OverlappingPath.Builder>()
        fun subPath(i: Int, j: Int): OverlappingPath.Builder {
            val builder = dpTable[Tuple2(i, j)].getOrElse {
                val muij = get(i, j)
                when {
                    muij <= Grade.FALSE -> OverlappingPath.Builder(this)
                    i == 0 || j == 0 -> OverlappingPath.Builder(this, muij, Array.of(Tuple2(i, j)))
                    else -> Array.of(subPath(i - 1, j), subPath(i, j - 1), subPath(i - 1, j - 1))
                            .maxBy { b -> b.grade  }
                            .map { it.extendPath(i, j) }
                            .get()
                }
            }
            dpTable = dpTable.put(Tuple2(i, j), builder)
            return builder
        }
        return Stream.concat((0..rowLastIndex).map { i ->  subPath(i, columnLastIndex) }, (0..columnLastIndex).map { j -> subPath(rowLastIndex, j) })
                .flatMap { it.build() }
                .maxBy { b -> evaluatePath(b) }.let { optionWhen(it.isDefined) { it.get() }  }
    }
}

