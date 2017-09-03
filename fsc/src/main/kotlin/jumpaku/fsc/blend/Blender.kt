package jumpaku.fsc.blend

import io.vavr.API
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.function2
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.apache.commons.math3.linear.MatrixUtils


enum class OverlappingCase{
    ExistingOverlapping,
    OverlappingExisting,
    ExistingOverlappingExisting,
    OverlappingExistingOverlapping,
}

data class OverlappingPath(
        val grade: Grade,
        val path: Array<Tuple2<Int, Int>>) {

    fun extend(grade: Grade, i: Int, j: Int): OverlappingPath {
        return when {
            (this.grade and grade) <= Grade.FALSE -> emptyPath()
            else -> OverlappingPath(this.grade and grade, path.append(Tuple2(i, j)))
        }
    }

    fun isEmpty(): Boolean = path.isEmpty

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

class OverlappingMatrix(samplingSpan: Double, val existing: BSpline, val overlapping: BSpline){

    val existingTimes: Array<Double> = existing.domain.sample(samplingSpan)

    val overlappingTimes: Array<Double> = overlapping.domain.sample(samplingSpan)

    private val computeGrade: Function2<Int, Int, Grade> = function2 {
        i: Int, j: Int -> existing(existingTimes[i]).isPossible(overlapping(overlappingTimes[j]))
    }.memoized()

    operator fun get(i: Int, j: Int): Grade = computeGrade.apply(i, j)
}

data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val blended: Option<BSpline>)

fun ovelappingCase(path: OverlappingPath, osm: OverlappingMatrix): OverlappingCase {
    require(path.nonEmpty()) { "empty path" }
    val existingLastIndex = osm.existingTimes.size() - 1
    val overlappingLastIndex = osm.overlappingTimes.size() - 1
    val (beginI, beginJ) = path.path.head()
    val (endI, endJ) = path.path.last()

    return when{
        beginI == 0 && endI == existingLastIndex -> OverlappingCase.ExistingOverlappingExisting
        beginI == 0 && endJ == overlappingLastIndex -> OverlappingCase.ExistingOverlapping
        beginJ == 0 && endI == existingLastIndex -> OverlappingCase.OverlappingExisting
        beginJ == 0 && endJ == overlappingLastIndex -> OverlappingCase.OverlappingExistingOverlapping
        else -> error("invalid path(${path.path}) (osm(${existingLastIndex - 1}x${overlappingLastIndex - 1}))")
    }
}

class Blender(
        val samplingSpan: Double = 1.0/128,
        val blendingRate: Double = 0.5,
        val evaluatePath: (OverlappingPath, OverlappingMatrix) -> Double = { (grade, _), _ -> grade.value }
) {

    /*fun blend(existing: BSpline, overlapping: BSpline): BlendResult {
        val osm = OverlappingMatrix(samplingSpan, existing, overlapping)
        val path = searchPath(osm)
        return BlendResult(osm, path, path.map { blend(osm, it) })
    }

    fun blend(osm: OverlappingMatrix, path: OverlappingPath): BSpline {
        require(path.nonEmpty()) { "empty overlapping path" }

        val (beginI, beginJ) = path.path.head()
        val (endI, endJ) = path.path.last()

        val et = osm.existingTimes
        val existingFront = osm.existing.subdivide(et[beginI])._1
        val existingOverlap = osm.existing.restrict(et[beginI], et[endI])
        val existingBack = osm.existing.subdivide(et[endI])._2

        val ot = osm.overlappingTimes
        val ovelappingFront = osm.overlapping.subdivide(ot[beginJ])._1
        val overlappingOverlap = osm.overlapping.restrict(ot[beginJ], ot[endJ])
        val overlappingBack = osm.overlapping.subdivide(ot[endJ])

        when(ovelappingCase(path, osm)){
            OverlappingCase.ExistingOverlapping -> {

            }
            OverlappingCase.OverlappingExisting -> {

            }
            OverlappingCase.ExistingOverlappingExisting -> {

            }
            OverlappingCase.OverlappingExistingOverlapping -> {

            }
        }
    }
*/
    fun searchPath(osm: OverlappingMatrix): Option<OverlappingPath> {

        var dpTable = HashMap.empty<Tuple2<Int, Int>, OverlappingPath>()
        fun subPath(i: Int, j: Int): OverlappingPath {
            val path =  dpTable[Tuple2(i, j)].getOrElse {
                val uij = osm[i, j]
                when {
                    i == 0 || j == 0 -> initialPath(uij, i, j)
                    else -> Array.of(subPath(i - 1, j), subPath(i, j - 1), subPath(i - 1, j - 1))
                            .maxBy { path -> evaluatePath(path, osm) }
                            .map { it.extend(uij, i, j) }
                            .get()
                }
            }
            dpTable = dpTable.put(Tuple2(i, j), path)
            return path
        }
        return Stream.concat(
                osm.existingTimes
                        .zipWithIndex { _, i ->  subPath(i, osm.overlappingTimes.size() - 1) }
                        .filter { it.nonEmpty() },
                osm.overlappingTimes
                        .zipWithIndex { _, j -> subPath(osm.existingTimes.size() - 1, j) }
                        .filter { it.nonEmpty() })
                .maxBy { path -> evaluatePath(path, osm) }
    }
}
