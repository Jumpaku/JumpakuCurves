package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.fit.weighted
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.abs

class Blender(
        val samplingSpan: Double = 0.01,
        val blendingRate: Double = 0.5,
        val threshold: Grade = Grade.FALSE) : ToJson {

    init {
        require(samplingSpan > 0.0)
        require(blendingRate in 0.0..1.0)
    }

    fun blend(existing: BSpline, overlapping: BSpline): Option<BlendData> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val overlap = detectOverlap(osm)
        return overlap.map { resample(existSamples, overlapSamples, it) }
    }

    fun detectOverlap(osm: OverlapMatrix): Option<OverlapState> {
        val distMaxRidge = findRidge(osm, compareBy { it.dist }) { i, j -> osm[i, j] > threshold }
                .map { it.subRidge.map { it.asPair() } }
                .filter { it.isNotEmpty() }
                .orNull() ?: return None
        val available = collectRange(osm, distMaxRidge, threshold)
        val grade = findRidge(osm, compareBy { it.grade }) { i, j -> osm[i, j] > threshold && (i to j) in available }
                .map { it.grade }.orThrow()
        val gradeMaxRidge = findRidge(osm, compareBy { it.dist }) { i, j -> osm[i, j] >= grade && (i to j) in available }
                .map { it.subRidge.map { it.asPair() } }
                .orThrow()
        val range = collectRange(osm, gradeMaxRidge, threshold)
        return  Some(OverlapState(osm, grade, gradeMaxRidge, range))
    }

    fun resample(existing: List<ParamPoint>, overlapping: List<ParamPoint>, overlapState: OverlapState): BlendData {
        val blendedData = overlapState.range
                .map { (i, j) -> existing[i].lerp(blendingRate, overlapping[j]).weighted(overlapState.osm[i, j].value) }
                .sortedBy { it.param }

        val (ridgeBeginI, ridgeBeginJ) = overlapState.ridge.first()
        val ridgeBeginExist = existing[ridgeBeginI].param
        val ridgeBeginOverlap = overlapping[ridgeBeginJ].param
        val frontExistCount = (0 until ridgeBeginI)
                .takeWhile { (it to 0) !in overlapState.range }.size
        val eFront = existing.take(frontExistCount)
                .map { it.run { copy(param = param + blendingRate * (ridgeBeginOverlap - ridgeBeginExist)) } }
        val frontOverlapCount = (0 until ridgeBeginJ)
                .takeWhile { (0 to it) !in overlapState.range }.size
        val oFront = overlapping.take(frontOverlapCount)
                .map { it.run { copy(param = param - (1 - blendingRate) * (ridgeBeginOverlap - ridgeBeginExist)) } }

        val (ridgeEndI, ridgeEndJ) = overlapState.ridge.last()
        val ridgeEndExist = existing[ridgeEndI].param
        val ridgeEndOverlap = overlapping[ridgeEndJ].param
        val backExistCount = (existing.lastIndex downTo (ridgeEndI + 1))
                .takeWhile { (it to overlapping.lastIndex) !in overlapState.range }.size
        val eBack = existing.takeLast(backExistCount)
                .map { it.run { copy(param = param + blendingRate * (ridgeEndOverlap - ridgeEndExist)) } }
        val backOverlapCount = (overlapping.lastIndex downTo (ridgeEndJ + 1))
                .takeWhile { (existing.lastIndex to it) !in overlapState.range }.size
        val oBack = overlapping.takeLast(backOverlapCount)
                .map { it.run { copy(param = param - (1 - blendingRate) * (ridgeEndOverlap - ridgeEndExist)) } }

        return BlendData(overlapState.grade, eFront + oFront, eBack + oBack, blendedData)
    }


    override fun toJson(): JsonElement = jsonObject(
            "samplingSpan" to samplingSpan.toJson(),
            "blendingRate" to blendingRate.toJson(),
            "threshold" to threshold.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        data class DpKey(val i: Int, val j: Int) {
            fun asPair(): Pair<Int, Int> = i to j
        }

        class DpValue(val grade: Grade, val dist: Int, val subRidge: List<DpKey>) {
            fun extend(key: DpKey, keyGrade: Grade): DpValue {
                val extended = subRidge + key
                val (fi, fj) = extended.first()
                val (li, lj) = extended.last()
                return DpValue(grade and keyGrade, abs(li - fi) + abs(lj - fj), extended)
            }
        }

        fun findRidge(osm: OverlapMatrix, compare: Comparator<DpValue>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            val dpTable = LinkedHashMap<DpKey, Option<DpValue>>()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                val muij = osm[i, j]
                when {
                    !isAvailable(i, j) -> none()
                    i == 0 && j == 0 -> some(DpValue(muij, 0, listOf(key)))
                    i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key, muij) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key, muij) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, muij) } }
                            .maxWith(compare).toOption()
                }
            }

            val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
            val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
            return (right + bottom).flatMap { dpSearch(it) }.maxWith(compare).toOption()
        }


        fun collectRange(osm: OverlapMatrix, ridge: List<Pair<Int, Int>>, threshold: Grade): Set<Pair<Int, Int>> {
            if (ridge.isEmpty()) return emptySet()

            // Left Bottom
            val frontLB = ridge.first()
                    .let { (i, j) -> ((j - 1) downTo 0).takeWhile { osm[i, it] > threshold }.map { i to it } }
            val backLB = ridge.last()
                    .let { (i, j) -> ((i + 1)..osm.rowLastIndex).takeWhile { osm[it, j] > threshold }.map { it to j } }
            val pLB = (frontLB + ridge + backLB).toSet()
            val dpTableLB = HashMap<Pair<Int, Int>, Boolean>()
            fun isAvailableLB(key: Pair<Int, Int>): Boolean = dpTableLB.getOrPut(key) {
                val (i, j) = key
                when {
                    (key in pLB) -> true
                    i > 0 && j < osm.columnLastIndex && osm[i, j] > threshold ->
                        setOf((i - 1) to j, i to (j + 1), (i - 1) to (j + 1))
                                .run { all { isAvailableLB(it) } || any { it in pLB } }
                    else -> false
                }
            }

            // Right Above
            val dpTableRA = HashMap<Pair<Int, Int>, Boolean>()
            val frontRA = ridge.first()
                    .let { (i, j) -> ((i - 1) downTo 0).takeWhile { osm[it, j] > threshold }.map { it to j } }
            val backRA = ridge.last()
                    .let { (i, j) -> ((j + 1)..osm.columnLastIndex).takeWhile { osm[i, it] > threshold }.map { i to it } }
            val pRA = (frontRA + ridge + backRA).toSet()
            fun isAvailableRA(key: Pair<Int, Int>): Boolean = dpTableRA.getOrPut(key) {
                val (i, j) = key
                when {
                    (key in pRA) -> true
                    i < osm.rowLastIndex && j > 0 && osm[i, j] > threshold ->
                        setOf((i + 1) to j, i to (j - 1), (i + 1) to (j - 1))
                                .run { all { isAvailableRA(it) } || any { it in pRA } }
                    else -> false
                }
            }

            return (0..osm.rowLastIndex).flatMap { i ->
                (0..osm.columnLastIndex).mapNotNull { j ->
                    (i to j).takeIf { isAvailableLB(it) || isAvailableRA(it) }
                }
            }.toSet()
        }

        fun fromJson(json: JsonElement): Blender = Blender(
                json["samplingSpan"].double,
                json["blendingRate"].double,
                Grade.fromJson(json["threshold"].asJsonPrimitive))
    }
}