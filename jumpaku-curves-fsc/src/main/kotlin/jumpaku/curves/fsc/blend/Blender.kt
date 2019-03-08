package jumpaku.curves.fsc.blend

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.some
import jumpaku.commons.control.toOption
import kotlin.math.abs


class Blender(
        val samplingSpan: Double = 0.025,
        val blendingRate: Double = 0.5,
        val minPossibility: Grade = Grade(1e-10),
        val evaluatePath: (path: OverlapPath, osm: OverlapMatrix) -> Double = { path, _ -> path.grade.value }) {

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<ParamPoint>> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val paths = findPaths(osm, minPossibility)
        val path = paths.maxBy { evaluatePath(it, osm) }.toOption()
        return path.map { resample(existSamples, overlapSamples, it) }
    }

    fun findPaths(osm: OverlapMatrix, possibilityThreshold: Grade): List<OverlapPath> {
        data class DpKey(val i: Int, val j: Int) {
            fun dist(key: DpKey): Int = key.let { abs(i - it.i) + abs(j - it.j) }
        }
        class DpValue(val dist: Int, val grade: Grade, val gradeSum: Double, val nodes: List<DpKey>) {
            fun extend(key: DpKey): DpValue {
                val d = dist + key.dist(nodes.last())
                val mu = grade and key.run { osm[i, j] }
                val sum = gradeSum + key.run { osm[i, j] }.value
                val l = nodes + key
                return DpValue(d, mu, sum, l)
            }
        }
        val compare = compareBy<DpValue> ({ it.dist }, { it.grade }, { it.gradeSum })
        val dpTable = LinkedHashMap<DpKey, Option<DpValue>>(osm.rowSize*osm.columnSize)
        fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
            val (i, j) = key
            val muij = osm[i, j]
            when {
                muij < possibilityThreshold -> none()
                i == 0 && j == 0 -> some(DpValue(0, muij, muij.value, listOf(key)))
                i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key) } + DpValue(0, muij, muij.value, listOf(key)))
                        .maxWith(compare).toOption()
                j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key) } + DpValue(0, muij, muij.value, listOf(key)))
                        .maxWith(compare).toOption()
                else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                        .flatMap { dpSearch(it).map { value -> value.extend(key) } }
                        .maxWith(compare).toOption()
            }
        }
        val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
        val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
        return (right + bottom).flatMap {
            dpSearch(it).map { value ->
                val elements = value.nodes.map { e -> e.i to e.j }
                val type = OverlapType.judgeType(osm.rowSize, osm.columnSize, elements)
                OverlapPath(type, value.grade, elements)
            }
        }
    }

    fun resample(existing: List<ParamPoint>, overlapping: List<ParamPoint>, path: OverlapPath): List<ParamPoint> {
        val (beginI, beginJ) = path.first()
        val (endI, endJ) = path.last()

        fun OverlapPath.blendData(te: List<ParamPoint>, to: List<ParamPoint>): List<ParamPoint> =
                map { (i, j) -> te[i].lerp(blendingRate, to[j]) }

        return when(path.type){
            OverlapType.ExistOverlap ->
                rearrangeParam(existing.take(beginI), path.blendData(existing, overlapping), overlapping.drop(endJ))
            OverlapType.OverlapExist ->
                rearrangeParam(overlapping.take(beginJ), path.blendData(existing, overlapping), existing.drop(endI))
            OverlapType.ExistOverlapExist ->
                rearrangeParam(existing.take(beginI), path.blendData(existing, overlapping), existing.drop(endI))
            OverlapType.OverlapExistOverlap ->
                rearrangeParam(overlapping.take(beginJ), path.blendData(existing, overlapping), overlapping.drop(endJ))
        }
    }

    fun rearrangeParam(front: List<ParamPoint>, middle: List<ParamPoint>, back: List<ParamPoint>): List<ParamPoint> {
        val f = when {
            front.isEmpty() -> front
            front.size == 1 -> front.map { it.copy(param = middle.first().param - samplingSpan) }
            else -> {
                val fEnd = middle.first().param - samplingSpan
                val span = front.run { last().param - first().param }
                val fBegin = (fEnd - span).coerceAtMost(fEnd)
                transformParams(front, range = Interval(fBegin, fEnd))
            }
        }
        val b = when {
            back.isEmpty() -> back
            back.size == 1 -> back.map { it.copy(param = middle.last().param + samplingSpan) }
            else -> {
                val bBegin = middle.last().param + samplingSpan
                val span = back.run { last().param - first().param }
                val bEnd = (bBegin + span).coerceAtLeast(bBegin)
                transformParams(back, range = Interval(bBegin, bEnd))
            }
        }
        return f + middle + b
    }
}
